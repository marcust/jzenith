package org.jzenith.core;

import com.englishtown.vertx.guice.GuiceVerticleFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import lombok.NonNull;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.util.Constants;
import org.jzenith.core.metrics.JZenithDefaultExports;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JZenith {

    static {
        System.setProperty(Constants.LOG4J_CONTEXT_SELECTOR, AsyncLoggerContextSelector.class.getName());
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        JZenithDefaultExports.initialize();
    }

    // Manually (not Lombok) after static block to ensure that the property for the context selector has been set.
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JZenith.class);

    private final LinkedList<AbstractPlugin> plugins = Lists.newLinkedList();
    private final LinkedList<Module> modules = Lists.newLinkedList();

    private final Configuration.ConfigurationBuilder configurationBuilder;

    public JZenith(Configuration.ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public static JZenith application(@NonNull String... args) {
        //Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> log.error("Uncaught exception", throwable));
        return new JZenith(Configuration.builder().commandLineArguments(Arrays.asList(args)));
    }

    public JZenith withPlugins(@NonNull AbstractPlugin... modules) {
        Preconditions.checkArgument(modules.length > 0, "You need to provide a module");

        this.plugins.addAll(Arrays.asList(modules));

        return this;
    }

    public JZenith bind(int port) {
        configurationBuilder.port(port);

        return this;
    }

    public Configuration.ConfigurationBuilder configuration() {
        return configurationBuilder;
    }

    public void run() {
        run(new JsonObject());
    }

    public void run(@NonNull final JsonObject vertxOptionsJson) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith starting up\nOptions: {}", vertxOptionsJson.encode());
        }
        final VertxOptions vertxOptions = new VertxOptions(vertxOptionsJson);

        final Vertx vertx = Vertx.vertx(vertxOptions);
        final Injector injector = createInjector(vertx);
        StreamEx.of(vertx.verticleFactories())
                .select(GuiceVerticleFactory.class)
                .findFirst()
                .ifPresent(guiceVerticleFactory -> guiceVerticleFactory.setInjector(injector));

        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(new JsonObject());
        deploymentOptions.getConfig().put("guice_binder", new JsonArray());

        final CompletableFuture[] deploymentResults = plugins.stream()
                .map(plugin -> plugin.start(injector))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(deploymentResults)
                    .get();
        } catch (Exception e) {
           Throwables.throwIfUnchecked(e);
           throw new RuntimeException(e);
        }

        log.debug("jZenith startup complete");
    }

    private Injector createInjector(Vertx vertx) {
        final List<Module> allModules = ImmutableList.<Module>builder()
                .add(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Vertx.class).toInstance(vertx);
                        bind(io.vertx.reactivex.core.Vertx.class).toInstance(io.vertx.reactivex.core.Vertx.newInstance(vertx));
                    }
                })
                .addAll(plugins.stream().flatMap(plugins -> plugins.getModules().stream()).collect(ImmutableList.toImmutableList()))
                .addAll(modules)
                .build();

        return Guice.createInjector(allModules);
    }

    @SafeVarargs
    public final JZenith withModules(AbstractModule... modules) {
        this.modules.addAll(Arrays.asList(modules));

        return this;
    }
}
