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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;
import javax.sql.DataSource;

public class MigrationVerticle extends AbstractVerticle {

    @Inject
    private DataSource dataSource;

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.executeBlocking(promise -> {
            // Create the Flyway instance
            final Flyway flyway = Flyway.configure()
                    .mixed(true)
                    .connectRetries(3)
                    .dataSource(dataSource)
                    .load();

            // Start the migration
            promise.complete(flyway.migrate());
        }, result -> {
            if (result.failed()) {
                startPromise.fail(result.cause());
            } else {
                startPromise.complete(null);
            }
        });


    }
}
