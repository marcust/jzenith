package org.jzenith.rest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.guice.ModuleProcessor;
import org.jboss.resteasy.plugins.server.vertx.VertxRegistry;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.rest.metrics.PrometheusResource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RestPlugin extends AbstractPlugin {

    private static final ImmutableList<Module> MODULES = ImmutableList.of(new RestBinder());

    private final List<Class<?>> resources;

    public RestPlugin(Collection<Class<?>> resources) {
        this.resources = ImmutableList.copyOf(Iterables.concat(resources, ImmutableList.of(PrometheusResource.class)));
    }

    public static RestPlugin withResources(Class<?>... resources) {
        return new RestPlugin(Arrays.asList(resources).stream()
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    protected List<Module> getModules() {
        return MODULES;
    }

    @Override
    protected CompletableFuture<String> start(Injector injector) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith Rest is starting and registering the following resources:\n{}", Joiner.on('\n').join(resources));
        }
        final VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.start();
        final ResteasyProviderFactory providerFactory = deployment.getProviderFactory();
        final VertxRegistry registry = deployment.getRegistry();
        final ModuleProcessor processor = new ModuleProcessor(registry, providerFactory);

        processor.processInjector(injector);

        resources.forEach(registry::addPerInstanceResource);

        final CompletableFuture<String> completableFuture = new CompletableFuture<>();

        final Vertx vertx = injector.getInstance(Vertx.class);

        vertx.createHttpServer()
                .requestHandler(new VertxRequestHandler(vertx, deployment))
                .listen(8080, ar -> {
                    if (ar.succeeded()) {
                        log.info("jZenith Server started on port " + ar.result().actualPort());
                        completableFuture.complete("Done");
                    } else {
                        completableFuture.completeExceptionally(ar.cause());
                    }
                });


        return completableFuture;
    }
}
