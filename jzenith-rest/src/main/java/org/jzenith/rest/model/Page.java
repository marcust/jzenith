package org.jzenith.rest.model;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@AllArgsConstructor
public class Page<T> {

    private final int offset;
    private final int limit;
    private final long totalElements;
    private final List<T> elements;


    public <U> Page<U> map(Function<T, U> mapper) {
        return new Page<>(offset,limit,totalElements,
                    elements.stream()
                            .map(mapper)
                            .collect(ImmutableList.toImmutableList()));
    }
}
