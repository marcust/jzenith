package org.jzenith.sql;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.Configuration;
import org.jzenith.core.util.CompletableHandler;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SqlPlugin extends AbstractPlugin {

    private final SqlConfiguration.SqlConfigurationBuilder configurationBuilder;

    private SqlPlugin(SqlConfiguration.SqlConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public static SqlPlugin forDataSource(Class<? extends DataSource> dataSourceClass) {
        return new SqlPlugin(SqlConfiguration.builder().dataSourceClass(dataSourceClass));
    }

    public SqlPlugin username(String username) {
        configurationBuilder.username(username);

        return this;
    }

    public SqlPlugin password(String password) {
        configurationBuilder.password(password);

        return this;
    }

    @Override
    protected List<AbstractModule> getModules() {
        return ImmutableList.of(new SqlBinder(configurationBuilder.build()));
    }

    @Override
    protected CompletableFuture<String> start(Vertx vertx, Configuration configuration, DeploymentOptions deploymentOptions) {
        if (log.isDebugEnabled()) {
            log.debug("jZenith SQL is starting");
        }
        final DeploymentOptions localDeploymentOptions = new DeploymentOptions(deploymentOptions);

        final CompletableHandler<String> completableHandler = new CompletableHandler<>();
        vertx.deployVerticle("java-guice:" + MigrationVerticle.class.getName(), localDeploymentOptions, completableHandler.handler());

        return completableHandler;
    }


}
