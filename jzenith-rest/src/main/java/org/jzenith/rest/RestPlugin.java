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
package org.jzenith.rest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.rxjava2.TracingRxJava2Utils;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.guice.GuiceResourceFactory;
import org.jboss.resteasy.plugins.server.vertx.VertxRegistry;
import org.jboss.resteasy.plugins.server.vertx.VertxResourceFactory;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableHandler;
import org.jzenith.rest.docs.CustomOpenApiResource;
import org.jzenith.rest.exception.ConstantMessageExceptionMapping;
import org.jzenith.rest.exception.ExceptionMapping;
import org.jzenith.rest.exception.ValidationExceptionMapping;
import org.jzenith.rest.health.HealthCheckResource;
import org.jzenith.rest.metrics.MetricsFeature;
import org.jzenith.rest.metrics.PrometheusResource;

import javax.validation.ValidationException;
import javax.ws.rs.container.DynamicFeature;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RestPlugin extends AbstractPlugin {

    private static final ImmutableList<Module> MODULES = ImmutableList.of(new RestBinder());
    private static final ImmutableList<Class<?>> DEFAULT_RESOURCES = ImmutableList.of(PrometheusResource.class, CustomOpenApiResource.class, HealthCheckResource.class);

    private final List<Class<?>> resources;
    private final Map<Class<? extends Exception>, ExceptionMapping<?>> exceptionMappings = Maps.newHashMap();

    public RestPlugin(Collection<Class<?>> resources) {
        this.resources = ImmutableList.copyOf(Iterables.concat(resources, DEFAULT_RESOURCES));

        exceptionMappings.put(Exception.class, new ConstantMessageExceptionMapping<>(Exception.class, 500, "Unknown error"));
        exceptionMappings.put(ValidationException.class, new ValidationExceptionMapping());
    }

    public static RestPlugin withResources(Class<?>... resources) {
        return new RestPlugin(Arrays.asList(resources).stream()
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.copyOf(Iterables.concat(MODULES, ImmutableList.of(new ResourceModule(resources))));
    }

    @Override
    protected CompletableFuture<String> start(Injector injector) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith Rest is starting and registering the following resources:\n{}", Joiner.on('\n').join(resources));
        }
        initRxJavaTracing();

        final GuiceVertxRequestHandler handler = initResteasy(injector);


        final RestConfiguration restConfiguration = injector.getInstance(RestConfiguration.class);
        final Vertx vertx = injector.getInstance(Vertx.class);

        final CompletableHandler<String> completableHandler = new CompletableHandler<>();
        vertx.deployVerticle(() -> new AbstractVerticle() {
                    @Override
                    public void start(Future<Void> startFuture) {
                        vertx.createHttpServer()
                                .requestHandler(handler)
                                .listen(restConfiguration.getPort(), restConfiguration.getHost(), ar -> {
                                    if (ar.succeeded()) {
                                        startFuture.complete(null);
                                    } else {
                                        startFuture.fail(ar.cause());
                                    }
                                });
                    }
                }, new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors()),
                completableHandler.handler());

        return completableHandler.thenApply(aVoid -> {
            log.debug("jZenith Rest Plugin started (listening to {}:{})",
                    restConfiguration.getHost(), restConfiguration.getPort());
            return "Done";
        });
    }

    private void initRxJavaTracing() {
        if (GlobalTracer.isRegistered()) {
            final Tracer tracer = GlobalTracer.get();

            TracingRxJava2Utils.enableTracing(tracer);
        }
    }

    private GuiceVertxRequestHandler initResteasy(@NonNull final Injector injector) {
        final Vertx vertx = injector.getInstance(Vertx.class);
        final VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.start();
        final ResteasyProviderFactory providerFactory = deployment.getProviderFactory();

        providerFactory.getServerDynamicFeatures().add(new MetricsFeature(injector.getInstance(MeterRegistry.class)));

        if (GlobalTracer.isRegistered()) {
            final DynamicFeature tracing = new ServerTracingDynamicFeature.Builder(GlobalTracer.get())
                    .build();
            providerFactory.getServerDynamicFeatures().add(tracing);
        }

        exceptionMappings.forEach((clz, exceptionMapping) -> providerFactory.getExceptionMappers().put(clz, exceptionMapping.toExceptionHandler()));

        final VertxRegistry registry = deployment.getRegistry();

        resources.forEach(resourceClass ->
                registry.addResourceFactory(new VertxResourceFactory(new GuiceResourceFactory(injector.getProvider(resourceClass), resourceClass))));

        providerFactory.registerProviderInstance(new JacksonConfig());
        return new GuiceVertxRequestHandler(vertx, deployment);
    }

    public RestPlugin withMapping(@NonNull final Class<? extends Exception> exception, int statusCode) {
        exceptionMappings.put(exception, new ExceptionMapping<>(exception, statusCode));

        return this;
    }

    public RestPlugin withMapping(@NonNull final Class<? extends Exception> exception, int statusCode, @NonNull String message) {
        exceptionMappings.put(exception, new ConstantMessageExceptionMapping<>(exception, statusCode, message));

        return this;
    }
}
