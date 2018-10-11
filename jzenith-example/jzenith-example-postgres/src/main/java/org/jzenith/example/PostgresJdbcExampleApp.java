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
package org.jzenith.example;

import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.contrib.reporter.slf4j.Slf4jReporter;
import io.opentracing.noop.NoopTracerFactory;
import org.jzenith.core.JZenith;
import org.jzenith.example.mapper.MapperModule;
import org.jzenith.example.persistence.PersistenceLayerModule;
import org.jzenith.example.resources.HelloWorldResource;
import org.jzenith.example.resources.UserResource;
import org.jzenith.example.service.ServiceLayerModule;
import org.jzenith.example.service.exception.NoSuchUserThrowable;
import org.jzenith.jdbc.JdbcDatabaseType;
import org.jzenith.jdbc.JdbcPlugin;
import org.jzenith.rest.RestPlugin;
import org.jzenith.rest.tracing.RequestScopedScopeManager;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.LoggerFactory;

public class PostgresJdbcExampleApp {

    private static JZenith application;

    public static void main(String... args) {
        application = configureApplication(args).run();
    }

    public static JZenith configureApplication(String... args) {
        final PGSimpleDataSource dataSource = createDataSource();

        return JZenith.application(args)
                .withTracer(new TracerR(NoopTracerFactory.create(), new Slf4jReporter(LoggerFactory.getLogger("opentracing"), true), new RequestScopedScopeManager()))
                .withPlugins(
                        RestPlugin.withResources(HelloWorldResource.class, UserResource.class)
                                  .withMapping(NoSuchUserThrowable.class, 404),
                        JdbcPlugin.create(dataSource, JdbcDatabaseType.POSTGRES)
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule(), new MapperModule());
    }

    private static PGSimpleDataSource createDataSource() {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setPortNumber(5433);
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("test");
        dataSource.setUser("test");
        dataSource.setPassword("test");
        return dataSource;
    }

    public static void stop() {
        application.stop();
    }
}
