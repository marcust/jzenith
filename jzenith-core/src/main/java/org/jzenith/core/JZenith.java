package org.jzenith.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

import java.util.Arrays;
import java.util.LinkedList;

public class JZenith {

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
        final VertxOptions vertxOptions = new VertxOptions(vertxOptionsJson);

        final Vertx vertx = Vertx.vertx(vertxOptions);

        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(new JsonObject());
        deploymentOptions.getConfig().put("guice_binder", new JsonArray().add(CoreBinder.class.getName()));

        final Configuration configuration = configurationBuilder.build();

        modules.forEach(module -> module.register(vertx, configuration, new DeploymentOptions(deploymentOptions)));

    }
}
