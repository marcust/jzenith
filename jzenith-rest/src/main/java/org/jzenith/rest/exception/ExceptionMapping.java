package org.jzenith.rest.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jzenith.rest.model.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Data
@AllArgsConstructor
public class ExceptionMapping {

    @NonNull
    private final Class<? extends Exception> exception;

    private final int statusCode;

    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            final WebApplicationException webApplicationException = (WebApplicationException) exception;

            return makeResponse(webApplicationException, webApplicationException.getResponse().getStatus());
        }

        return makeResponse(exception, statusCode);
    }

    private Response makeResponse(Throwable exception, int statusCode) {
        return Response.status(statusCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(statusCode, exception.getMessage()))
                .build();
    }

    public ExceptionMapper toExceptionHandler() {
        return exception -> toResponse(exception);
    }
}
