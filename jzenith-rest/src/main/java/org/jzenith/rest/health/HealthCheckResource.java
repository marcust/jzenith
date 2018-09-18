/**
 * Copyright (C) 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.rest.health;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;
import org.jzenith.core.health.HealthState;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("/health")
public class HealthCheckResource {

    private final Set<HealthCheck> healthChecks;

    @Inject
    public HealthCheckResource(Set<HealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Single<Response> doHealthChecks() {
        return Observable.fromIterable(healthChecks)
                .flatMapSingle(healthCheck -> healthCheck.execute())
                .toList()
                .map(this::toResponse);
    }

    private Response toResponse(List<HealthCheckResult> healthCheckResults) {
        final boolean isDown = healthCheckResults.stream().anyMatch(HealthCheckResult::isDown);

        final HealthCheckResponse response = new HealthCheckResponse(isDown ? HealthState.DOWN : HealthState.UP, healthCheckResults);

        if (isDown) {
            return Response.status(500).entity(response).build();
        } else {
            return Response.status(200).entity(response).build();
        }
    }

}
