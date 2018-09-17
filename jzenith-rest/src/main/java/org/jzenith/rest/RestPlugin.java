package org.jzenith.rest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.impl.HttpServerRequestImpl;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.guice.GuiceResourceFactory;
import org.jboss.resteasy.plugins.server.vertx.VertxRegistry;
import org.jboss.resteasy.plugins.server.vertx.VertxResourceFactory;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.rest.exception.ConstantMessageExceptionMapping;
import org.jzenith.rest.exception.ExceptionMapping;
import org.jzenith.rest.metrics.MetricsFeature;
import org.jzenith.rest.metrics.PrometheusResource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RestPlugin extends AbstractPlugin {

    private static final ImmutableList<Module> MODULES = ImmutableList.of(new RestBinder());

    private final List<Class<?>> resources;
    private final Map<Class<? extends Exception>, ExceptionMapping> exceptionMappings = Maps.newHashMap();

    public RestPlugin(Collection<Class<?>> resources) {
        this.resources = ImmutableList.copyOf(Iterables.concat(resources, ImmutableList.of(PrometheusResource.class)));

        exceptionMappings.put(Exception.class, new ConstantMessageExceptionMapping(Exception.class, 500, "Unknown error"));
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
        final VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.start();
        final ResteasyProviderFactory providerFactory = deployment.getProviderFactory();

        providerFactory.getServerDynamicFeatures().add(new MetricsFeature());
        exceptionMappings.forEach((clz, exceptionMapping) -> providerFactory.getExceptionMappers().put(clz, exceptionMapping.toExceptionHandler()));

        final VertxRegistry registry = deployment.getRegistry();

        resources.forEach(resourceClass ->
            registry.addResourceFactory(new VertxResourceFactory(new GuiceResourceFactory(injector.getProvider(resourceClass), resourceClass))));

        final CompletableFuture<String> completableFuture = new CompletableFuture<>();

        final Vertx vertx = injector.getInstance(Vertx.class);
        final RestConfiguration restConfiguration = injector.getInstance(RestConfiguration.class);

        final GuiceVertxRequestHandler handler = new GuiceVertxRequestHandler(vertx, deployment);
        vertx.createHttpServer()
                .requestHandler(handler)
                .listen(restConfiguration.getPort(), restConfiguration.getHost(), ar -> {
                    if (ar.succeeded()) {
                        final HttpServer server = ar.result();
                        log.info("jZenith Server started on port " + server.actualPort());
                        completableFuture.complete("Done");
                    } else {
                        completableFuture.completeExceptionally(ar.cause());
                    }
                });


        return completableFuture;
    }

    public RestPlugin withMapping(@NonNull Class<? extends Exception> exception, int statusCode) {
        exceptionMappings.put(exception, new ExceptionMapping(exception, statusCode));

        return this;
    }

    public RestPlugin withMapping(@NonNull Class<? extends Exception> exception, int statusCode, @NonNull String message) {
        exceptionMappings.put(exception, new ConstantMessageExceptionMapping(exception, statusCode, message));

        return this;
    }
}
