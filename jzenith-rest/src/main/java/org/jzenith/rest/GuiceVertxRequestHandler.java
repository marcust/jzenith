package org.jzenith.rest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class GuiceVertxRequestHandler extends VertxRequestHandler {
    public GuiceVertxRequestHandler(Vertx vertx, ResteasyDeployment deployment) {
        super(vertx, deployment);
    }

    @Override
    public void handle(HttpServerRequest request) {
        ResteasyProviderFactory.getContextDataMap().put(HttpServerRequest.class, request);
        super.handle(request);
    }
}
