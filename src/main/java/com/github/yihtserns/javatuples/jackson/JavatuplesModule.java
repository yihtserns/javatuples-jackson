/*
 * Copyright 2022 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.javatuples.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.javatuples.Decade;
import org.javatuples.Ennead;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Septet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import org.javatuples.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JavatuplesModule extends SimpleModule {

    public JavatuplesModule() {
        addDeserializer(Unit.class, Unit::fromCollection);
        addDeserializer(Pair.class, Pair::fromCollection);
        addDeserializer(Triplet.class, Triplet::fromCollection);
        addDeserializer(Quartet.class, Quartet::fromCollection);
        addDeserializer(Quintet.class, Quintet::fromCollection);
        addDeserializer(Sextet.class, Sextet::fromCollection);
        addDeserializer(Septet.class, Septet::fromCollection);
        addDeserializer(Octet.class, Octet::fromCollection);
        addDeserializer(Ennead.class, Ennead::fromCollection);
        addDeserializer(Decade.class, Decade::fromCollection);

        addSerializer(Tuple.class, new TupleSerializer());
    }

    private <T extends Tuple> void addDeserializer(Class<T> tupleType, Function<Collection<?>, T> collectionToTuple) {
        addDeserializer(tupleType, new TupleDeserializer<>(tupleType, collectionToTuple));
    }

    private static class TupleDeserializer<T extends Tuple> extends StdDeserializer<T> implements ContextualDeserializer {

        private final Function<Collection<?>, T> collectionToTuple;
        private final List<JavaType> entryTypes;

        public TupleDeserializer(Class<T> tupleType, Function<Collection<?>, T> collectionToTuple) {
            this(tupleType, collectionToTuple, null);
        }

        private TupleDeserializer(Class<T> tupleType, Function<Collection<?>, T> collectionToTuple, List<JavaType> entryTypes) {
            super(tupleType);
            this.collectionToTuple = collectionToTuple;
            this.entryTypes = entryTypes;
        }

        @Override
        public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.isExpectedStartArrayToken()) {
                return (T) context.handleUnexpectedToken(handledType(), parser);
            }

            ArrayNode arrayNode = parser.readValueAsTree();
            if (arrayNode.size() != entryTypes.size()) {
                throw new InvalidFormatException(
                        parser,
                        String.format("Expected JSON array of size %s, but was: %s", entryTypes.size(), arrayNode),
                        arrayNode,
                        handledType());
            }

            List<?> entries = new ArrayList<>();
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode jsonNode = arrayNode.get(i);
                JavaType entryType = entryTypes.get(i);

                entries.add(context.readTreeAsValue(jsonNode, entryType));
            }

            return collectionToTuple.apply(entries);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            List<JavaType> entryTypes = context.getContextualType().getBindings().getTypeParameters();
            if (entryTypes.isEmpty()) {
                entryTypes = Arrays.stream(handledType().getTypeParameters())
                        .map(type -> context.getTypeFactory().constructType(type))
                        .collect(Collectors.toList());
            }

            return new TupleDeserializer<>((Class<T>) handledType(), collectionToTuple, entryTypes);
        }
    }

    private static class TupleSerializer extends StdSerializer<Tuple> {

        public TupleSerializer() {
            super(Tuple.class, false);
        }

        @Override
        public void serialize(Tuple tuple, JsonGenerator generator, SerializerProvider provider) throws IOException {
            JsonSerializer<Object> listSerializer = provider.findValueSerializer(List.class);

            listSerializer.serialize(tuple.toList(), generator, provider);
        }
    }
}
