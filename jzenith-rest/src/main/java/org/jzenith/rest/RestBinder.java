package org.jzenith.rest;

import com.englishtown.vertx.guice.GuiceJerseyBinder;
import com.englishtown.vertx.jersey.ApplicationConfigurator;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Modules;
import org.jzenith.rest.metrics.MetricsPostResponseProcessor;
import org.jzenith.rest.metrics.MetricsRequestProcessor;
import org.jzenith.rest.metrics.MetricsResponseProcessor;

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

            Multibinder.newSetBinder(binder(), VertxRequestProcessor.class).addBinding().to(MetricsRequestProcessor.class);
            Multibinder.newSetBinder(binder(), VertxResponseProcessor.class).addBinding().to(MetricsResponseProcessor.class);
            Multibinder.newSetBinder(binder(), VertxPostResponseProcessor.class).addBinding().to(MetricsPostResponseProcessor.class);

        }


    }

}
