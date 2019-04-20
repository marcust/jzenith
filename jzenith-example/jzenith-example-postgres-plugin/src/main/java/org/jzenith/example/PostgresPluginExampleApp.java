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

import org.jzenith.core.JZenith;
import org.jzenith.example.mapper.MapperModule;
import org.jzenith.example.persistence.PersistenceLayerModule;
import org.jzenith.example.resources.HelloWorldResource;
import org.jzenith.example.resources.UserResource;
import org.jzenith.example.service.ServiceLayerModule;
import org.jzenith.example.service.exception.NoSuchUserThrowable;
import org.jzenith.postgresql.PostgresqlPlugin;
import org.jzenith.rest.RestPlugin;
import org.jzenith.rest.tracing.LoggingTracer;

public class PostgresPluginExampleApp {

    private static JZenith application;

    public static void main(String... args) {
        application = configureApplication(args).run();
    }

    public static JZenith configureApplication(String... args) {
        return JZenith.application(args)
                .withTracer(new LoggingTracer())
                .withPlugins(
                        RestPlugin.withResources(HelloWorldResource.class, UserResource.class)
                                  .withMapping(NoSuchUserThrowable.class, 404),
                        PostgresqlPlugin.create()
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule(), new MapperModule())
                .withConfiguration("postgresql.port", 5433)
                .withConfiguration("postgresql.database", "test")
                .withConfiguration("postgresql.username", "test")
                .withConfiguration("postgresql.password", "test");
    }

    public static void stop() {
        application.stop();
    }

}
