package org.jzenith.rest.metrics;

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import io.vertx.core.http.HttpServerResponse;
import org.glassfish.jersey.server.ContainerResponse;

import java.time.Instant;

public class MetricsResponseProcessor implements VertxResponseProcessor {

    public static final String FIRST_BYTE_TIME_CONTEXT_KEY = "jzenith.metrics.firstByteTime";

    @Override
    public void process(final HttpServerResponse vertxResponse, final ContainerResponse jerseyResponse) {
        jerseyResponse.getRequestContext().setProperty(FIRST_BYTE_TIME_CONTEXT_KEY, Instant.now());
    }
}
