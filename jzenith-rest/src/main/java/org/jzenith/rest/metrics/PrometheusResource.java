package org.jzenith.rest.metrics;

import com.google.common.collect.ImmutableSet;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.io.output.StringBuilderWriter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@Path("/metrics")
public class PrometheusResource {

    private final CollectorRegistry registry;

    private final HttpServerRequest httpServerRequest;
    private final Vertx vertx;

    @Inject
    public PrometheusResource(final HttpServerRequest httpServerRequest,
                              final Vertx vertx) {
        this.registry = CollectorRegistry.defaultRegistry;

        this.httpServerRequest = httpServerRequest;
        this.vertx = vertx;
    }

    @GET
    @Path("/prometheus")
    @Produces(TextFormat.CONTENT_TYPE_004)
    public void prometheusEndpoint(@Suspended final AsyncResponse asyncResponse) {
        final Set<String> includedNames = ImmutableSet.copyOf(httpServerRequest.params().getAll("name"));
        vertx.executeBlocking(future -> {
                    try {
                        try (Writer writer = new StringBuilderWriter()) {
                            TextFormat.write004(writer, registry.filteredMetricFamilySamples(includedNames));
                            writer.flush();
                            future.complete(writer.toString());
                        }
                    } catch (IOException e) {
                        future.fail(e);
                    }

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
