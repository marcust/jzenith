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
package org.jzenith.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import lombok.NonNull;
import org.davidmoten.rx.jdbc.Database;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.jzenith.core.health.HealthCheck;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

class JdbcBinder extends AbstractModule {

    @NonNull
    private final DataSource dataSource;

    JdbcBinder(@NonNull final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void configure() {
        bind(DataSource.class).toInstance(dataSource);

        configurePool();

        bind(JdbcClient.class).in(Singleton.class);

        bind(JdbcConfiguration.class).toProvider(new ConfigurationProvider<>(JdbcConfiguration.class)).in(Singleton.class);

        bind(DSLContext.class).toProvider(new DSLContextProvider()).in(Singleton.class);

        final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        healthCheckMultibinder.addBinding().to(JdbcHealthCheck.class);
    }

    private void configurePool() {
        bind(Database.class).toProvider(new DatabaseProvider()).in(Singleton.class);
    }


    private static class DatabaseProvider implements Provider<Database> {

        @Inject
        private JdbcConfiguration configuration;

        @Inject
        private DataSource dataSource;

        @Override
        public Database get() {
            return Database
                    .nonBlocking()
                    // the jdbc url of the connections to be placed in the pool
                    .connectionProvider(dataSource)
                    // an unused connection will be closed after thirty minutes
                    .maxIdleTime(30, TimeUnit.MINUTES)
                    // connections are checked for healthiness on checkout if the connection
                    // has been idle for at least 5 seconds
                    .healthCheck(configuration.getDatabaseType().getType())
                    .idleTimeBeforeHealthCheck(5, TimeUnit.SECONDS)
                    // if a connection fails creation then retry after 30 seconds
                    .connectionRetryInterval(30, TimeUnit.SECONDS)
                    // the maximum number of connections in the pool
                    .maxPoolSize(configuration.getPoolSize())
                    .build();
        }
    }

    private static class DSLContextProvider implements Provider<DSLContext> {

        @Inject
        private JdbcConfiguration configuration;

        @Override
        public DSLContext get() {
            final DSLContext context = DSL.using(configuration.getDatabaseType().getDialect());

            // Initialize Jooq on startup, because that takes a while
            context.select().from("1").getSQL();

            return context;

        }
    }
}
