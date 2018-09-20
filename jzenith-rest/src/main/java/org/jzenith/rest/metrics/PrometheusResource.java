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
package org.jzenith.rest.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Path("/metrics")
public class PrometheusResource {


    private final HttpServerRequest httpServerRequest;
    private final Vertx vertx;
    private final PrometheusMeterRegistry registry;

    @Inject
    public PrometheusResource(final HttpServerRequest httpServerRequest,
                              final Vertx vertx,
                              final MeterRegistry registry) {
        this.registry = (PrometheusMeterRegistry)registry;


        this.httpServerRequest = httpServerRequest;
        this.vertx = vertx;
    }

    @GET
    @Path("/prometheus")
    @Produces(TextFormat.CONTENT_TYPE_004)
    public void prometheusEndpoint(@Suspended final AsyncResponse asyncResponse) {
        vertx.executeBlocking(future -> {
                    future.complete(registry.scrape());
                },
                result -> {
                    if (result.failed()) {
                        asyncResponse.resume(result.cause());
                    } else {
                        asyncResponse.resume(result.result());
                    }
                    return;
                }
        );


    }
}
