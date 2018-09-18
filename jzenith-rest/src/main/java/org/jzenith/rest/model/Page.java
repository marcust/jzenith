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
package org.jzenith.rest.model;

import com.google.common.collect.ImmutableList;
import lombok.*;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page<T> {

    private int offset;
    private int limit;
    private long totalElements;

    @NonNull
    private List<T> elements;

    public <U> Page<U> map(Function<T, U> mapper) {
        return new Page<>(offset,limit,totalElements,
                    elements.stream()
                            .map(mapper)
                            .collect(ImmutableList.toImmutableList()));
    }
}
