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
package org.jzenith.mongodb;

import com.google.inject.Module;
import io.opentracing.noop.NoopTracerFactory;
import org.jzenith.core.JZenith;
import org.testcontainers.containers.GenericContainer;

public abstract class AbstractMongoDbPluginTest {

    public static GenericContainer container = new GenericContainer("mongo:4")
            .withExposedPorts(27017);

    static {
        container.start();
    }


    JZenith makeApplication(Module... modules) {
        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(MongoDbPlugin.create("mongodb://localhost:" + container.getFirstMappedPort()))
                .withModules(modules);
    }

}
