package org.jzenith.core;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public abstract class AbstractModule {

    protected abstract void register(Vertx vertx, Configuration configuration, DeploymentOptions config);


}
