package org.jzenith.rest.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

public class PrometheusListener implements ApplicationEventListener {

    private static final Counter REQUEST_COUNTER = Counter.build()
            .name("jersey_request_total")
            .help("Total requests to the service")
            .labelNames("resourceName", "methodName", "method", "status")
            .register();

    private static final Histogram TIME_TO_FIRST_BYTE = Histogram.build()
            .name("jersey_request_time_to_first_byte")
            .labelNames("resourceName", "methodName", "method", "status")
            .help("Time to first byte in seconds.")
            .buckets(0.005D, 0.01D, 0.05D, 0.1D, 0.25D, 0.5D, 0.75D, 1.0D, 2.0D, 3.0D, 4.0, 5.0D, 9.99D)
            .register();

    private static final Histogram REQUEST_DURATION = Histogram.build()
            .name("jersey_request_duration_seconds")
            .labelNames("resourceName", "methodName", "method", "status")
            .help("Request duration in seconds.")
            .buckets(0.005D, 0.01D, 0.05D, 0.1D, 0.25D, 0.5D, 0.75D, 1.0D, 2.0D, 3.0D, 4.0, 5.0D, 9.99D)
            .register();

    public static void track(final String resourceName,
                             final String methodName,
                             final String requestMethod,
                             final int httpStatus,
                             final long timeToFirstByteMillis,
                             final long requestDurationMillis) {

        final String statusString = String.valueOf(httpStatus);

        REQUEST_COUNTER.labels(resourceName, methodName, requestMethod, statusString).inc();
        REQUEST_DURATION.labels(resourceName, methodName, requestMethod, statusString).observe(requestDurationMillis / 1000.0D);

        if (timeToFirstByteMillis > 0L) {
            TIME_TO_FIRST_BYTE.labels(resourceName, methodName, requestMethod, statusString).observe(timeToFirstByteMillis / 1000.0D);
        }
    }

    @Override
    public void onEvent(final ApplicationEvent event) {
        // do nothing, we do not listen to application events
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        final Instant startTime = Instant.now();

        return event -> {
            if (RequestEvent.Type.FINISHED.equals(event.getType())) {
                final ExtendedUriInfo uriInfo = event.getUriInfo();
                if (uriInfo == null) {
                    return;
                }

                final ResourceMethod matchedResourceMethod = uriInfo.getMatchedResourceMethod();
                if (matchedResourceMethod == null) {
                    return;
                }

                final Invocable invocable = matchedResourceMethod.getInvocable();
                if (invocable == null) {
                    return;
                }

                final Method handlingMethod = invocable.getHandlingMethod();
                if (handlingMethod == null) {
                    return;
                }

                final String handlingMethodName = handlingMethod.getName();
                final String resourceClassName = handlingMethod.getDeclaringClass().getSimpleName();

                final ContainerResponse response = event.getContainerResponse();
                if (response == null) {
                    return;
                }

                final ContainerRequest requestContext = response.getRequestContext();

                final Instant timeOfFirstByte = (Instant) requestContext.getProperty(MetricsResponseProcessor.FIRST_BYTE_TIME_CONTEXT_KEY);
                final Instant now = Instant.now();
                final long requestDurationMillis = Duration.between(startTime, now).toMillis();
                final long timeToFirstByteMillis = timeOfFirstByte != null ? Duration.between(timeOfFirstByte, now).toMillis() : 0L;

                final ContainerRequest request = event.getContainerRequest();

                track(resourceClassName,
                        handlingMethodName,
                        request.getMethod(),
                        response.getStatus(),
                        timeToFirstByteMillis,
                        requestDurationMillis);
            }
        };
    }

}
