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
package org.jzenith.mongodb;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.jzenith.core.health.HealthCheck;

class MongoDbBinder extends AbstractModule {

    @Override
    protected void configure() {
        bind(MongoDbConfiguration.class).toProvider(new ConfigurationProvider<>(MongoDbConfiguration.class)).asEagerSingleton();
        bind(MongoClient.class).toProvider(new MongoClientProvider()).asEagerSingleton();

        final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        healthCheckMultibinder.addBinding().to(MongoDbHealthCheck.class);
    }

    private static class MongoClientProvider implements Provider<MongoClient> {

        @Inject
        private MongoDbConfiguration configuration;

        @Inject
        private Vertx vertx;

        @Override
        public MongoClient get() {
            final JsonObject config = new JsonObject();
            config.put("db_name", configuration.getDatabaseName());
            config.put("connection_string", configuration.getConnectString());

            return MongoClient.createShared(vertx, config);
        }
    }
}
