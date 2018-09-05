package org.jzenith.rest;

import com.englishtown.vertx.guice.GuiceJerseyBinder;
import com.englishtown.vertx.jersey.ApplicationConfigurator;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import io.vertx.core.Vertx;

public class RestBinder extends AbstractModule {

    @Override
    protected void configure() {
        install(Modules.override(new GuiceJerseyBinder()).with(new Overrides()));
    }

    public class Overrides extends AbstractModule {

        @Override
        protected void configure() {
            bind(ApplicationConfigurator.class).to(ResourceConfigurator.class);
        }


    }

    private class ResourceConfiguratorProvider implements Provider<ApplicationConfigurator> {

        private final Vertx vertx;

        @Inject
        private ResourceConfiguratorProvider(Vertx vertx) {
            this.vertx = vertx;
        }

        @Override
        public ApplicationConfigurator get() {
            return new ResourceConfigurator(vertx);
        }
    }
}
