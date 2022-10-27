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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.javatuples.Unit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JavatuplesModule extends SimpleModule {

    public JavatuplesModule() {
        addDeserializer(Unit.class, new UnitDeserializer());
        addSerializer(Unit.class, new UnitSerializer());
    }

    private static class UnitDeserializer extends StdDeserializer<Unit<?>> {

        public UnitDeserializer() {
            super(Unit.class);
        }

        @Override
        public Unit<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            List<?> list = parser.readValueAs(List.class);

            return Unit.with(list.get(0));
        }
    }

    private static class UnitSerializer extends StdSerializer<Unit> {

        public UnitSerializer() {
            super(Unit.class, false);
        }

        @Override
        public void serialize(Unit unit, JsonGenerator generator, SerializerProvider provider) throws IOException {
            JsonSerializer<Object> listSerializer = provider.findValueSerializer(List.class);

            listSerializer.serialize(
                    Collections.singletonList(unit.getValue0()),
                    generator,
                    provider);
        }
    }
}
