package org.jzenith.rest.exception;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;

public class ValidationExceptionMapping extends ExceptionMapping<ValidationException> {

    public ValidationExceptionMapping() {
        super(ValidationException.class, Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Override
    public Response toResponse(ValidationException exception) {
        return super.toResponse(exception);
    }
}
