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
