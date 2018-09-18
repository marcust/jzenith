package org.jzenith.rest.exception;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jzenith.rest.model.ErrorResponse;

import javax.ws.rs.core.Response;

@SuppressFBWarnings(value = "EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC", justification = "Lombok foobar")
@EqualsAndHashCode
public class ConstantMessageExceptionMapping<T extends Exception> extends ExceptionMapping<T> {

    private final Response errorResponse;

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "Lombok foobar")
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
