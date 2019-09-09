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
package org.jzenith.postgresql;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.reactiverse.pgclient.PgPoolOptions;
import io.reactiverse.reactivex.pgclient.PgClient;
import io.reactiverse.reactivex.pgclient.PgPool;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.jzenith.core.health.HealthCheck;

import javax.inject.Inject;
import javax.inject.Provider;

class PostgresqlBinder extends AbstractModule {


    PostgresqlBinder() {
    }

    @Override
    protected void configure() {
        configurePgPool();

        bind(PostgresqlClient.class).in(Singleton.class);

        bind(PostgresqlConfiguration.class).toProvider(new ConfigurationProvider<>(PostgresqlConfiguration.class));
        final DSLContext context = DSL.using(SQLDialect.POSTGRES);

        // Initialize Jooq on startup, because that takes a while
        context.select().from("1").getSQL();

        bind(DSLContext.class).toInstance(context);

        final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        healthCheckMultibinder.addBinding().to(PostgresqlHealthCheck.class);
    }

    private void configurePgPool() {
        bind(PgPool.class).toProvider(new PgPoolProvider()).in(Singleton.class);
    }


    private static class PgPoolProvider implements Provider<PgPool> {

        @Inject
        private PostgresqlConfiguration configuration;

        @Override
        public PgPool get() {
            final PgPoolOptions options = new PgPoolOptions()
                    .setPort(configuration.getPort())
                    .setHost(configuration.getHost())
                    .setDatabase(configuration.getDatabase())
                    .setUser(configuration.getUsername())
                    .setPassword(configuration.getPassword())
                    .setMaxSize(configuration.getPoolSize())
                    .setConnectTimeout(1000);

            final PgPool pool = PgClient.pool(options);

            // warm up the pool
            pool.rxQuery("select 1;").subscribe();

            return pool;
        }
    }
}
