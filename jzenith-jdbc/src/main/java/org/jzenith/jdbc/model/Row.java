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

import com.google.common.collect.Iterables;
import lombok.NonNull;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jooq.Field;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Row {

    private final Map<String, Object> rowMap;

    private Row(@NonNull Map<String, Object> rowMap) {
        this.rowMap = new CaseInsensitiveMap<>(rowMap);
    }

    public static Row fromMap(@NonNull final Map<String, Object> rowMap) {
        return new Row(rowMap);
    }

    public <T> T getColumn(@NonNull final String columnName, @NonNull final Class<T> type) {
        return type.cast(rowMap.get(columnName));
    }

    public UUID getUUID(@NonNull final String columnName) {
        return getColumn(columnName, UUID.class);
    }

    public String getString(@NonNull final String columnName) {
        return getColumn(columnName, String.class);
    }

    public Long getOnlyLong() {
        return getOnlyValue(Long.class);
    }

    private Long getOnlyValue(@NonNull final Class<Long> type) {
        return type.cast(Iterables.getOnlyElement(rowMap.values()));
    }

    public <T> T get(@NonNull final Field<T> field) {
        return getColumn(field.getName(), field.getType());
    }

    public <T,U> U get(@NonNull Field<T> field, final Function<T,U> converter) {
        return converter.apply(get(field));
    }
}
