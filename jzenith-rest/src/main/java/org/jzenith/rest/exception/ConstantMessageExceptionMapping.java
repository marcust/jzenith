package org.jzenith.rest.exception;

import lombok.NonNull;
import org.jzenith.rest.model.ErrorResponse;

import javax.ws.rs.core.Response;

public class ConstantMessageExceptionMapping<T extends Exception> extends ExceptionMapping<T> {

    private final Response errorResponse;

    public ConstantMessageExceptionMapping(@NonNull Class<T> exception, int statusCode, @NonNull String message) {
        super(exception, statusCode);
        this.errorResponse = Response.status(statusCode).entity(new ErrorResponse(statusCode, message)).build();
    }

    @Override
    public Response toResponse(T exception) {
        if (exception.getClass() == this.getExceptionType()) {
            return errorResponse;
        } else {
            return super.toResponse(exception);
        }
    }
}
