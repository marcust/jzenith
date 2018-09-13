package org.jzenith.postgresql;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;

import javax.inject.Inject;

public class MigrationVerticle extends AbstractVerticle {

    @Inject
    private PostgresqlConfiguration configuration;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.executeBlocking(future -> {
            final PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setPortNumber(configuration.getPort());
            dataSource.setServerName(configuration.getHost());
            dataSource.setDatabaseName(configuration.getDatabase());
            dataSource.setUser(configuration.getUsername());
            dataSource.setPassword(configuration.getPassword());

            // Create the Flyway instance
            final Flyway flyway = new Flyway();

            // Point it to the database
            flyway.setDataSource(dataSource);

            // Start the migration
            future.complete(flyway.migrate());
        }, result -> {
            if (result.failed()) {
                startFuture.fail(result.cause());
            } else {
                startFuture.complete(null);
            }
        });


    }
}
