package org.jzenith.rest.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import javax.ws.rs.container.*;
import javax.ws.rs.core.Request;
import java.io.IOException;
import java.lang.reflect.Method;

public class MetricsInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Counter REQUEST_COUNTER = Counter.build()
            .name("rest_request_total")
            .help("Total requests to the service")
            .labelNames("resourceName", "methodName", "method", "status")
            .register();

    private static final Histogram REQUEST_DURATION = Histogram.build()
            .name("rest_request_duration_seconds")
            .labelNames("resourceName", "methodName", "method", "status")
            .help("Request duration in seconds.")
            .buckets(0.005D, 0.01D, 0.05D, 0.1D, 0.25D, 0.5D, 0.75D, 1.0D, 2.0D, 3.0D, 4.0, 5.0D, 9.99D)
            .register();
    private static final String START_TIME_PROPERTY = "jzenith.startTime";

    private final String resourceName;
    private final String methodName;

    public MetricsInterceptor(final ResourceInfo resourceInfo) {
        resourceName = resourceInfo.getResourceClass().getSimpleName();
        methodName = resourceInfo.getResourceMethod().getName();
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final Request request = requestContext.getRequest();
        final String statusString = String.valueOf(responseContext.getStatus());
        REQUEST_COUNTER.labels(resourceName, methodName, request.getMethod(), statusString).inc();

        final Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
        REQUEST_DURATION.labels(resourceName, methodName, request.getMethod(), statusString).observe((System.currentTimeMillis() - startTime) / 1000D);


    }
}
