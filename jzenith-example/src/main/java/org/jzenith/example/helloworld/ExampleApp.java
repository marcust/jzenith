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
package org.jzenith.example.helloworld;

import org.jzenith.core.JZenith;
import org.jzenith.example.helloworld.mapper.MapperModule;
import org.jzenith.example.helloworld.persistence.PersistenceLayerModule;
import org.jzenith.example.helloworld.resources.HelloWorldResource;
import org.jzenith.example.helloworld.resources.UserResource;
import org.jzenith.example.helloworld.service.ServiceLayerModule;
import org.jzenith.example.helloworld.service.exception.NoSuchUserException;
import org.jzenith.jdbc.JdbcDatabaseType;
import org.jzenith.jdbc.JdbcPlugin;
import org.jzenith.rest.RestPlugin;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Example app for simple Rest ExampleApp
 */
public class ExampleApp {

    public static void main(String... args) {
        configureApplication(args).run();
    }

    public static JZenith configureApplication(String... args) {
        final PGSimpleDataSource dataSource = createDataSource();

        return JZenith.application(args)
                .withPlugins(
                        RestPlugin.withResources(HelloWorldResource.class, UserResource.class)
                                  .withMapping(NoSuchUserException.class, 404),
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
}
