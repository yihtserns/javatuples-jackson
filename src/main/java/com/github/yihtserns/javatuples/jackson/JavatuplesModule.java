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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.javatuples.Pair;
import org.javatuples.Unit;

import java.io.IOException;
import java.util.List;

public class JavatuplesModule extends SimpleModule {

    public JavatuplesModule() {
        addDeserializer(Unit.class, new UnitDeserializer());
        addSerializer(Unit.class, new UnitSerializer());

        addDeserializer(Pair.class, new PairDeserializer());
        addSerializer(Pair.class, new PairSerializer());
    }

    private static class UnitDeserializer extends StdDeserializer<Unit<?>> implements ContextualDeserializer {

        private JavaType entryType;

        public UnitDeserializer() {
            this(null);
        }

        private UnitDeserializer(JavaType entryType) {
            super(Unit.class);
            this.entryType = entryType;
        }

        @Override
        public Unit<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.isExpectedStartArrayToken()) {
                return (Unit) context.handleUnexpectedToken(Unit.class, parser);
            }

            ArrayNode arrayNode = parser.readValueAsTree();
            if (arrayNode.size() != 1) {
                throw new InvalidFormatException(
                        parser,
                        "Expected JSON array of size 1, but was: " + arrayNode,
                        arrayNode,
                        Unit.class);
            }

            return Unit.with(context.readTreeAsValue(arrayNode.get(0), entryType));
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            List<JavaType> entryTypes = context.getContextualType().getBindings().getTypeParameters();

            return new UnitDeserializer(entryTypes.get(0));
        }
    }

    private static class UnitSerializer extends StdSerializer<Unit> {

        public UnitSerializer() {
            super(Unit.class, false);
        }

        @Override
        public void serialize(Unit unit, JsonGenerator generator, SerializerProvider provider) throws IOException {
            JsonSerializer<Object> listSerializer = provider.findValueSerializer(List.class);

            listSerializer.serialize(unit.toList(), generator, provider);
        }
    }

    private static class PairDeserializer extends StdDeserializer<Pair<?, ?>> implements ContextualDeserializer {

        private List<JavaType> entryTypes;

        public PairDeserializer() {
            this(null);
        }

        public PairDeserializer(List<JavaType> entryTypes) {
            super(Pair.class);
            this.entryTypes = entryTypes;
        }

        @Override
        public Pair<?, ?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.isExpectedStartArrayToken()) {
                return (Pair) context.handleUnexpectedToken(Pair.class, parser);
            }

            ArrayNode arrayNode = parser.readValueAsTree();
            if (arrayNode.size() != 2) {
                throw new InvalidFormatException(
                        parser,
                        "Expected JSON array of size 2, but was: " + arrayNode,
                        arrayNode,
                        Pair.class);
            }

            return Pair.with(
                    context.readTreeAsValue(arrayNode.get(0), entryTypes.get(0)),
                    context.readTreeAsValue(arrayNode.get(1), entryTypes.get(1)));
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            return new PairDeserializer(context.getContextualType().getBindings().getTypeParameters());
        }
    }

    private static class PairSerializer extends StdSerializer<Pair> {

        protected PairSerializer() {
            super(Pair.class);
        }

        @Override
        public void serialize(Pair pair, JsonGenerator generator, SerializerProvider provider) throws IOException {
            JsonSerializer<Object> listSerializer = provider.findValueSerializer(List.class);

            listSerializer.serialize(pair.toList(), generator, provider);
        }
    }
}
