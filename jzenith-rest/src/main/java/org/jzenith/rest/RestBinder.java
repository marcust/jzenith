/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        bind(HttpServerRequest.class).toProvider(new ResteasyContextProvider<>(HttpServerRequest.class)).in(RequestScoped.class);
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
