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

import org.junit.jupiter.api.Test;
import org.jzenith.rest.model.ErrorResponse;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ThrowableMappingTest {

    @Test
    public void testIllegalArgumentException() {
        final ThrowableMapping<IllegalArgumentException> mapping = new ThrowableMapping<>(IllegalArgumentException.class, 404);

        final Response response = mapping.toResponse(new IllegalArgumentException("Something is wrong"));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
    }

    @Test
    public void testWebApplicationException() {
        final ThrowableMapping<WebApplicationException> mapping = new ThrowableMapping<>(WebApplicationException.class, 404);

        final Response response = mapping.toResponse(new NotFoundException());

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
    }

    @Test
    public void testConstantExceptionMapping() {
        final ThrowableMapping<WebApplicationException> mapping = new ConstantMessageThrowableMapping<>(WebApplicationException.class, 404, "test");

        final Response response = mapping.toResponse(new NotFoundException());

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
    }


}
