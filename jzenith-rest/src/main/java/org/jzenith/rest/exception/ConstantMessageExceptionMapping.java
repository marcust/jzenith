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
package org.jzenith.rest.exception;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jzenith.rest.model.ErrorResponse;

import javax.ws.rs.core.Response;

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
