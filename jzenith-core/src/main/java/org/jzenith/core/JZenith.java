package org.jzenith.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.util.Constants;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JZenith {

    static {
        System.setProperty(Constants.LOG4J_CONTEXT_SELECTOR, AsyncLoggerContextSelector.class.getName());
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
    }

    // Manually (not Lombok) after static block to ensure that the property for the context selector has been set.
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JZenith.class);

    private final LinkedList<AbstractModule> modules = Lists.newLinkedList();
    private final Configuration.ConfigurationBuilder configurationBuilder;

    public JZenith(Configuration.ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public static JZenith application(@NonNull String... args) {
        return new JZenith(Configuration.builder().commandLineArguments(Arrays.asList(args)));
    }

    public JZenith withModule(@NonNull AbstractModule... modules) {
        Preconditions.checkArgument(modules.length > 0, "You need to provide a module");

        this.modules.addAll(Arrays.asList(modules));

        return this;
    }

    public JZenith bind(int port) {
        configurationBuilder.port(port);

        return this;
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

        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(new JsonObject());
        deploymentOptions.getConfig().put("guice_binder", new JsonArray().add(CoreBinder.class.getName()));

        final Configuration configuration = configurationBuilder.build();

        final CompletableFuture[] deploymentResults = modules.stream()
                .map(module -> module.start(vertx, configuration, new DeploymentOptions(deploymentOptions)))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(deploymentResults)
                    .get(1, TimeUnit.MINUTES);
        } catch (Exception e) {
           Throwables.throwIfUnchecked(e);
           throw new RuntimeException(e);
        }

        log.debug("jZenith startup complete");
    }
}
