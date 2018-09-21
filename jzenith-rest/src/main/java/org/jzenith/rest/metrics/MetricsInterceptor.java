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
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.NonNull;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Request;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.Arrays.asList;

public class MetricsInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "jzenith.startTime";

    private final String resourceName;
    private final String methodName;
    private final MeterRegistry registry;

    public MetricsInterceptor(@NonNull final MeterRegistry registry, @NonNull final ResourceInfo resourceInfo) {
        resourceName = resourceInfo.getResourceClass().getSimpleName();
        methodName = resourceInfo.getResourceMethod().getName();
        this.registry = registry;
    }

    private Timer.Builder timer() {
        return Timer.builder("rest.request.duration")
                .description("Request duration seconds")
                .minimumExpectedValue(Duration.ofMillis(5))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .sla(Duration.ofMillis(5),
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofMillis(750),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(3000),
                        Duration.ofMillis(4000),
                        Duration.ofMillis(7500),
                        Duration.ofMillis(10000));
    }


    @Override
    public void filter(@NonNull ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final Request request = requestContext.getRequest();
        final String statusString = String.valueOf(responseContext.getStatus());

        final List<Tag> tags = asList(Tag.of("resourceName", resourceName), Tag.of("methodName", methodName),
                Tag.of("method", request.getMethod()), Tag.of("status", statusString));

        registry.counter("rest.request.total", tags).increment();

        final Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);

        timer().tags(tags).register(registry).record(Duration.of(System.currentTimeMillis() - startTime, ChronoUnit.MILLIS));


    }
}
