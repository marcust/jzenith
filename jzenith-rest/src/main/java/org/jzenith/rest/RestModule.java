package org.jzenith.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.jzenith.core.AbstractModule;
import org.jzenith.core.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RestModule extends AbstractModule {

    private final List<String> resources;

    public RestModule(Collection<String> resources) {
        this.resources = ImmutableList.copyOf(resources);
    }

    public static RestModule withResources(Class<?>... resources) {
        return new RestModule(Arrays.asList(resources).stream()
                .map(Class::getName)
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    protected void register(Vertx vertx, Configuration configuration, DeploymentOptions deploymentOptions) {
        final DeploymentOptions localDeploymentOptions = new DeploymentOptions(deploymentOptions);
        final JsonObject config = localDeploymentOptions.getConfig();

        config.getJsonArray("guice_binder").add(RestBinder.class.getName());

        if (StringUtils.isNotBlank(configuration.getHost())) {
            config.put("host", configuration.getHost());
        }

        config.put("port", configuration.getPort());
        config.put("components", new JsonArray().add("org.glassfish.jersey.jackson.JacksonFeature"));
        config.put("jzenith.resources", new JsonArray(resources));

        vertx.deployVerticle("java-guice:com.englishtown.vertx.jersey.JerseyVerticle", new DeploymentOptions(localDeploymentOptions));

    }
}
