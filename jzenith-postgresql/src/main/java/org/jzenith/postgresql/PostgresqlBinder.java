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

        bind(PostgresqlConfiguration.class).toProvider(new ConfigurationProvider(PostgresqlConfiguration.class));
        final DSLContext context = DSL.using(SQLDialect.POSTGRES_10);

        // Initialize Jooq on startup, because that takes a while
        context.select().from("1").getSQL();

        bind(DSLContext.class).toInstance(context);

        final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        healthCheckMultibinder.addBinding().to(PostgresHealthCheck.class);
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
                    .setMaxSize(configuration.getPoolSize());

            final PgPool pool = PgClient.pool(options);

            // warm up the pool
            pool.rxQuery("select 1;").subscribe();

            return pool;
        }
    }
}
