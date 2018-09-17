package org.jzenith.rest.exception;

import lombok.NonNull;
import org.jzenith.rest.model.ErrorResponse;

import javax.ws.rs.core.Response;

public class ConstantMessageExceptionMapping extends ExceptionMapping {

    private final Response errorResponse;

    public ConstantMessageExceptionMapping(@NonNull Class<? extends Exception> exception, int statusCode, @NonNull String message) {
        super(exception, statusCode);
        this.errorResponse = Response.status(statusCode).entity(new ErrorResponse(statusCode, message)).build();
    }

    @Override
    public Response toResponse(Throwable exception) {
        if (exception.getClass() == super.getException()) {
            return errorResponse;
        } else {
            return super.toResponse(exception);
        }
    }
}
