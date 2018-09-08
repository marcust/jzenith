package org.jzenith.rest;

import com.englishtown.vertx.jersey.ApplicationConfigurator;
import com.google.common.collect.ImmutableSet;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.glassfish.jersey.server.ResourceConfig;
import org.jzenith.core.util.ReflectionUtil;
import org.jzenith.rest.metrics.PrometheusListener;

import javax.inject.Inject;
import java.util.Set;

public class ResourceConfigurator implements ApplicationConfigurator {

    private final Vertx vertx;

    @Inject
    public ResourceConfigurator(Vertx vertx) {
        this.vertx = vertx;
    }


    @Override
    public ResourceConfig configure(ResourceConfig rc) {
        final JsonArray jsonArray = vertx.getOrCreateContext().config().getJsonArray(RestPlugin.RESOURCES_KEY);
        final Set<Class<?>> resources = jsonArray.stream().map(String.class::cast).map(ReflectionUtil::loadClass).collect(ImmutableSet.toImmutableSet());
        rc.registerClasses(resources);

        rc.register(PrometheusListener.class);
        return rc;
    }

}
