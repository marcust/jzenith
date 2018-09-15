package org.jzenith.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.jzenith.rest.metrics.MetricsFeature;

public class RestBinder extends AbstractModule {

    @Override
    protected void configure() {
        install(new RequestScopeModule());

        bind(HttpServerRequest.class).toProvider(new ResteasyContextProvider(HttpServerRequest.class)).in(RequestScoped.class);
        bind(RestConfiguration.class).toProvider(new ConfigurationProvider<>(RestConfiguration.class)).in(Singleton.class);
    }

    private static class ResteasyContextProvider<T> implements Provider<T> {

        private final Class<T> instanceClass;

        public ResteasyContextProvider(Class<T> instanceClass)
        {
            this.instanceClass = instanceClass;
        }

        @Override
        public T get() {
            return ResteasyProviderFactory.getContextData(instanceClass);
        }
    }
}
