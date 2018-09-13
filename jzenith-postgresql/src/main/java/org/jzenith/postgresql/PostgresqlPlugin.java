package org.jzenith.postgresql;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PostgresqlPlugin extends AbstractPlugin {

    private final PostgresqlConfiguration.PostgresqlConfigurationBuilder configurationBuilder;

    private PostgresqlPlugin(PostgresqlConfiguration.PostgresqlConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public static PostgresqlPlugin create() {
        return new PostgresqlPlugin(PostgresqlConfiguration.builder());
    }

    public PostgresqlPlugin username(String username) {
        configurationBuilder.username(username);

        return this;
    }

    public PostgresqlPlugin password(String password) {
        configurationBuilder.password(password);

        return this;
    }

    public PostgresqlPlugin port(int port) {
        configurationBuilder.port(port);

        return this;
    }

    public PostgresqlPlugin host(String host) {
        configurationBuilder.host(host);

        return this;
    }

    public PostgresqlPlugin database(String database) {
        configurationBuilder.database(database);

        return this;
    }

    public PostgresqlPlugin poolSize(int poolSize) {
        configurationBuilder.poolSize(poolSize);

        return this;
    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.of(new PostgresqlBinder(configurationBuilder.build()));
    }

    @Override
    protected CompletableFuture<String> start(Injector injector) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith SQL is starting");
        }

        final Vertx vertx = injector.getInstance(Vertx.class);

        final CompletableHandler<String> completableHandler = new CompletableHandler<>();
        vertx.deployVerticle("java-guice:" + MigrationVerticle.class.getName(), new DeploymentOptions(), completableHandler.handler());

        return completableHandler;
    }


}
