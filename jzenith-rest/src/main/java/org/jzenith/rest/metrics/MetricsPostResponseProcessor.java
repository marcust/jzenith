package org.jzenith.rest.metrics;

import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class MetricsPostResponseProcessor implements VertxPostResponseProcessor {

    private static final Joiner JOINER = Joiner.on(',');

    @Override
    public void process(final HttpServerResponse vertxResponse, final ContainerResponse jerseyResponse) {
        final ContainerRequest requestContext = jerseyResponse.getRequestContext();

        final Instant startTime = (Instant) requestContext.getProperty(MetricsRequestProcessor.START_TIME_CONTEXT_KEY);
        final Instant timeOfFirstByte = (Instant) requestContext.getProperty(MetricsResponseProcessor.FIRST_BYTE_TIME_CONTEXT_KEY);
        final Instant now = Instant.now();

        final URI requestUri = requestContext.getRequestUri();
        final String requestUrl = requestUri.toString();
        final int status = jerseyResponse.getStatus();
        final HttpMethod method = HttpMethod.valueOf(requestContext.getMethod());

        final long requestDurationMillis = Duration.between(startTime, now).toMillis();
        final long timeToFirstByteMillis = Duration.between(timeOfFirstByte, now).toMillis();

        log.info("Request metrics for {} {} status {}: {}ms",
                method, requestUrl, status, requestDurationMillis
                );
    }

    private Map<String, String> transformMap(final MultivaluedMap<String, String> originalHeaders) {
        final Map<String, String> headers = Maps.newTreeMap();
        for (final Map.Entry<String, List<String>> entry : originalHeaders.entrySet()) {
            headers.put(StringUtils.lowerCase(entry.getKey()), JOINER.join(entry.getValue()));
        }
        return headers;
    }

}
