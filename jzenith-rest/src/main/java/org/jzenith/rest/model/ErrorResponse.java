package org.jzenith.rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class ErrorResponse {

    private final int status;

    private final @NonNull String message;
}
