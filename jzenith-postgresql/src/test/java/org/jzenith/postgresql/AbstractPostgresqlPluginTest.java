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

import io.opentracing.noop.NoopTracerFactory;
import org.junit.ClassRule;
import org.jzenith.core.JZenith;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractPostgresqlPluginTest {

    @ClassRule
    public static PostgreSQLContainer container = new PostgreSQLContainer();

    JZenith makeApplication() {
        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(PostgresqlPlugin.create())
                .withConfiguration("postgresql.port", container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
                .withConfiguration("postgresql.database", container.getDatabaseName())
                .withConfiguration("postgresql.username", container.getUsername())
                .withConfiguration("postgresql.password", container.getPassword());
    }

}
