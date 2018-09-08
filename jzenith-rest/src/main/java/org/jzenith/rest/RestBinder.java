package org.jzenith.rest;

import com.englishtown.vertx.guice.GuiceJerseyBinder;
import com.englishtown.vertx.jersey.ApplicationConfigurator;
import com.englishtown.vertx.jersey.JerseyServer;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;

public class RestBinder extends AbstractModule {

    @Override
    protected void configure() {
        install(Modules.override(new GuiceJerseyBinder()).with(new Overrides()));
    }

    public class Overrides extends AbstractModule {

        @Override
        protected void configure() {
            bind(ApplicationConfigurator.class).to(ResourceConfigurator.class);
            bind(JerseyServer.class).to(CustomGuiceJerseyServer.class);
        }


    }

}
