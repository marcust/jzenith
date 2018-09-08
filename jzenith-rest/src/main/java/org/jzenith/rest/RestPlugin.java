package org.jzenith.rest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.Configuration;
import org.jzenith.core.util.CompletableHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RestPlugin extends AbstractPlugin {

    static final String RESOURCES_KEY = "jzenith.resources";

    private static final ImmutableList<AbstractModule> MODULES = ImmutableList.of(new RestBinder());

    private final List<String> resources;

    public RestPlugin(Collection<String> resources) {
        this.resources = ImmutableList.copyOf(resources);
    }

    public static RestPlugin withResources(Class<?>... resources) {
        return new RestPlugin(Arrays.asList(resources).stream()
                .map(Class::getName)
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    protected List<AbstractModule> getModules() {
        return MODULES;
    }

    @Override
    protected CompletableFuture<String> start(Vertx vertx, Configuration configuration, DeploymentOptions deploymentOptions) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith Rest is starting and registering the following resources:\n{}", Joiner.on('\n').join(resources));
        }
        final DeploymentOptions localDeploymentOptions = new DeploymentOptions(deploymentOptions);
        final JsonObject config = localDeploymentOptions.getConfig();

        if (StringUtils.isNotBlank(configuration.getHost())) {
            config.put("host", configuration.getHost());
        }

        config.put("port", configuration.getPort());
        config.put("components", new JsonArray().add("org.glassfish.jersey.jackson.JacksonFeature"));
        config.put(RESOURCES_KEY, new JsonArray(resources));

        final CompletableHandler<String> completableHandler = new CompletableHandler<>();
        vertx.deployVerticle("java-guice:com.englishtown.vertx.jersey.JerseyVerticle", new DeploymentOptions(localDeploymentOptions), completableHandler.handler());

        return completableHandler;
    }
}
