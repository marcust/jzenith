package org.jzenith.rest;

import com.englishtown.vertx.guice.GuiceJerseyServer;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServerOptions;
import com.englishtown.vertx.jersey.VertxContainer;
import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.inject.Provider;

public class CustomGuiceJerseyServer extends GuiceJerseyServer  {

    @Inject
    public CustomGuiceJerseyServer(JerseyHandler jerseyHandler, VertxContainer container, Provider<JerseyServerOptions> optionsProvider, ServiceLocator locator, Injector injector) {
        super(jerseyHandler, container, optionsProvider, locator, injector);
    }

    @Override
    protected void initBridge(ServiceLocator locator, Injector injector) {
        // Somehow the binding in GuiceJerseyServer for the GuiceContext leads
        // to the GuiceContext being registered twice. So we just set up the
        // bridge here:
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
        GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
        injectMultibindings(locator, injector);
    }
}
