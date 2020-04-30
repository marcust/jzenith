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
package org.jzenith.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.jzenith.core.health.HealthCheck;
import org.nustaq.serialization.FSTConfiguration;

import javax.inject.Inject;

class RedisBinder extends AbstractModule {


    RedisBinder() {
    }

    @Override
    protected void configure() {
        bind(RedisConfiguration.class).toProvider(new ConfigurationProvider<>(RedisConfiguration.class)).asEagerSingleton();
        bind(RedisConnection.class).toProvider(new RedisProvider()).asEagerSingleton();

        final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        healthCheckMultibinder.addBinding().to(RedisHealthCheck.class);

        bind(FSTConfiguration.class).toInstance(FSTConfiguration.createUnsafeBinaryConfiguration());
    }

    private static class RedisProvider implements Provider<RedisConnection> {

        @Inject
        private Vertx vertx;

        @Inject
        private RedisConfiguration configuration;


        @Override
        public RedisConnection get() {
            final RedisOptions options = new RedisOptions()
                    .setConnectionString("redis://" + configuration.getHost() + ":" + configuration.getPort());

            return Redis.createClient(vertx, options).rxConnect().blockingGet();
        }
    }
}
