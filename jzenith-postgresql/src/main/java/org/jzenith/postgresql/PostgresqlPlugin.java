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

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableFutureHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jzenith.core.util.VerticleDeploymentUtil.forGuiceVerticleLoader;

@Slf4j
public class PostgresqlPlugin extends AbstractPlugin {

    private PostgresqlPlugin() {
    }

    public static PostgresqlPlugin create() {
        return new PostgresqlPlugin();
    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.of(new PostgresqlBinder());
    }

    @Override
    protected CompletableFuture<String> start(Injector injector) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith PostgreSQL is starting");
        }

        final Vertx vertx = injector.getInstance(Vertx.class);

        final CompletableFutureHandler<String> completableFutureHandler = new CompletableFutureHandler<>();
        vertx.deployVerticle("java-guice:" + MigrationVerticle.class.getName(), forGuiceVerticleLoader(), completableFutureHandler.handler());

        return completableFutureHandler;
    }


}
