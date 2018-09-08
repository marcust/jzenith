package org.jzenith.rest.metrics;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.glassfish.jersey.server.ContainerRequest;

import java.time.Instant;

public class MetricsRequestProcessor implements VertxRequestProcessor {

    public static final String START_TIME_CONTEXT_KEY = "jzenith.metrics.startTime";

    @Override
    public void process(final HttpServerRequest vertxRequest, final ContainerRequest jerseyRequest, final Handler<Void> done) {
        jerseyRequest.setProperty(START_TIME_CONTEXT_KEY, Instant.now());
        done.handle(null);
    }
}
