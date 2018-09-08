package org.jzenith.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractPlugin {

    protected List<AbstractModule> getModules() {
        return ImmutableList.of();
    }

    protected abstract CompletableFuture<String> start(Vertx vertx, Configuration configuration, DeploymentOptions deploymentOptions);


}
