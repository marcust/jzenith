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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableFutureHandler;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.jzenith.core.util.VerticleDeploymentUtil.forGuiceVerticleLoader;

@Slf4j
public class JdbcPlugin extends AbstractPlugin {

    @NonNull
    private final DataSource dataSource;

    @NonNull
    private final JdbcDatabaseType type;

    private JdbcPlugin(@NonNull final DataSource dataSource, @NonNull final JdbcDatabaseType type) {
        this.dataSource = dataSource;
        this.type = type;
    }

    public static JdbcPlugin create(@NonNull final DataSource dataSource, @NonNull final JdbcDatabaseType type) {
        return new JdbcPlugin(dataSource, type);
    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.of(new JdbcBinder(dataSource));
    }

    @Override
    protected Map<String, Object> getExtraConfiguration() {
        return ImmutableMap.of("jdbc.database.type", type);
    }

    @Override
    protected CompletableFuture<String> start(Injector injector) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith JDBC is starting");
        }

        final Vertx vertx = injector.getInstance(Vertx.class);

        final CompletableFutureHandler<String> completableFutureHandler = new CompletableFutureHandler<>();
        vertx.deployVerticle("java-guice:" + MigrationVerticle.class.getName(), forGuiceVerticleLoader(), completableFutureHandler.handler());

        return completableFutureHandler;
    }


}
