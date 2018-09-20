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

import com.mysql.cj.jdbc.MysqlDataSource;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.contrib.reporter.slf4j.Slf4jReporter;
import io.opentracing.noop.NoopTracerFactory;
import org.jzenith.core.JZenith;
import org.jzenith.example.mapper.MapperModule;
import org.jzenith.example.persistence.PersistenceLayerModule;
import org.jzenith.example.resources.HelloWorldResource;
import org.jzenith.example.resources.UserResource;
import org.jzenith.example.service.ServiceLayerModule;
import org.jzenith.example.service.exception.NoSuchUserException;
import org.jzenith.jdbc.JdbcDatabaseType;
import org.jzenith.jdbc.JdbcPlugin;
import org.jzenith.rest.RestPlugin;
import org.jzenith.rest.tracing.RequestScopedScopeManager;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Example app for simple Rest ExampleApp
 */
public class ExampleApp {

    public static void main(String... args) throws SQLException {
        configureApplication(args).run();
    }

    public static JZenith configureApplication(String... args) throws SQLException {
        final DataSource dataSource = createDataSource();

        return JZenith.application(args)
                .withTracer(new TracerR(NoopTracerFactory.create(), new Slf4jReporter(LoggerFactory.getLogger("opentracing"), true), new RequestScopedScopeManager()))
                .withPlugins(
                        RestPlugin.withResources(HelloWorldResource.class, UserResource.class)
                                  .withMapping(NoSuchUserException.class, 404),
                        JdbcPlugin.create(dataSource, JdbcDatabaseType.MYSQL)
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule(), new MapperModule());
    }

    public static DataSource createDataSource() throws SQLException {
        final MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setPort(3307);
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("test");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        dataSource.setUseSSL(false);
        dataSource.setAllowPublicKeyRetrieval(true);
        return dataSource;
    }
}
