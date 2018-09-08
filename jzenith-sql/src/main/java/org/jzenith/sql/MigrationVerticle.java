package org.jzenith.sql;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;
import javax.sql.DataSource;

public class MigrationVerticle extends AbstractVerticle {

    @Inject
    private DataSource dataSource;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.executeBlocking(future -> {
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
