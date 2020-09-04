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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class MetricsInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_NANOS_PROPERTY = "jzenith.requestStartTime.nanos";
    private static final LoadingCache<Integer, String> STATUS_CODE_STRINGS = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, String>() {
                @Override
                public String load(Integer key) {
                    return key.toString();
                }
            });

    private static final Duration TIMER_MINIMUM_EXPECTED_DURATION = Duration.ofMillis(5);
    private static final Duration TIMER_MAXIMUM_EXPECTED_DURATION = Duration.ofSeconds(10);
    private static final Duration[] SLA_BUCKETS = {
            Duration.ofMillis(5),
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
            Duration.ofMillis(10000)
    };

    private final String resourceName;
    private final String methodName;
    private final MeterRegistry registry;
    private final Tag resourceNameTag;
    private final Tag methodNameTag;

    public MetricsInterceptor(@NonNull final MeterRegistry registry, @NonNull final ResourceInfo resourceInfo) {
        resourceName = resourceInfo.getResourceClass().getSimpleName();
        methodName = resourceInfo.getResourceMethod().getName();
        this.registry = registry;
        resourceNameTag = Tag.of("resourceName", resourceName);
        methodNameTag = Tag.of("methodName", methodName);
    }

    private Timer.Builder timer() {
        return Timer.builder("rest.request.duration")
                .description("Request duration seconds")
                .minimumExpectedValue(TIMER_MINIMUM_EXPECTED_DURATION)
                .maximumExpectedValue(TIMER_MAXIMUM_EXPECTED_DURATION)
                .serviceLevelObjectives(SLA_BUCKETS);
    }

    @Override
    public void filter(@NonNull ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_NANOS_PROPERTY, System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final Request request = requestContext.getRequest();
        final String statusString = STATUS_CODE_STRINGS.getUnchecked(responseContext.getStatus());

        final List<Tag> tags = asList(resourceNameTag, methodNameTag,
                Tag.of("method", request.getMethod()), Tag.of("status", statusString));

        registry.counter("rest.request.total", tags).increment();

        final Long startTimeNano = (Long) requestContext.getProperty(START_TIME_NANOS_PROPERTY);

        timer().tags(tags).register(registry).record(System.nanoTime() - startTimeNano, TimeUnit.NANOSECONDS);
    }
}
