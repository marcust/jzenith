/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.jdbc.model;

import org.jooq.Field;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RowTest {

    @Test
    public void testGetColumn() {
        final Row row = Row.fromMap(Map.of("string", "value"));

        final String value = row.getColumn("string", String.class);

        assertThat(value).isEqualTo("value");
    }

    @Test
    public void testGetString() {
        final Row row = Row.fromMap(Map.of("string", "value"));

        final String value = row.getString("string");

        assertThat(value).isEqualTo("value");
    }

    @Test
    public void testGetUUID() {
        final Row row = Row.fromMap(Map.of("uuid", UUID.randomUUID()));

        final UUID value = row.getUUID("uuid");

        assertThat(value).isNotNull();
    }

    @Test
    public void testGetOnlyLong() {
        final Row row = Row.fromMap(Map.of("string", Long.valueOf(5)));

        final Long value = row.getOnlyLong();

        assertThat(value).isEqualTo(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetField() {
        final Row row = Row.fromMap(Map.of("string", Long.valueOf(5)));
        final Field<Long> mock = mock(Field.class);
        when(mock.getName()).thenReturn("string");
        when(mock.getType()).thenReturn(Long.class);

        final String value = row.get(mock, Object::toString);

        assertThat(value).isEqualTo("5");
    }


}
