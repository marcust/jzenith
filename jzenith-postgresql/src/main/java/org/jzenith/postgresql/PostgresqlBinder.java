package org.jzenith.postgresql;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import io.reactiverse.pgclient.PgPoolOptions;
import io.reactiverse.reactivex.pgclient.PgClient;
import io.reactiverse.reactivex.pgclient.PgPool;

public class PostgresqlBinder extends AbstractModule {

    private final PostgresqlConfiguration configuration;

    public PostgresqlBinder(PostgresqlConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        configurePgPool();

        bind(PostgresqlClient.class).in(Singleton.class);
        bind(PostgresqlConfiguration.class).toInstance(configuration);
    }

    private void configurePgPool() {
        final PgPoolOptions options = new PgPoolOptions()
                .setPort(configuration.getPort())
                .setHost(configuration.getHost())
                .setDatabase(configuration.getDatabase())
                .setUser(configuration.getUsername())
                .setPassword(configuration.getPassword())
                .setMaxSize(configuration.getPoolSize());

        final PgPool client = PgClient.pool(options);

        bind(PgPool.class).toInstance(client);
    }


}
