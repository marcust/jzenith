package org.jzenith.core;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractModule {

    protected abstract CompletableFuture<String> start(Vertx vertx, Configuration configuration, DeploymentOptions config);


}
