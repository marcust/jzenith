package org.jzenith.postgresql;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableHandler;

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
            log.debug("jZenith SQL is starting");
        }

        final Vertx vertx = injector.getInstance(Vertx.class);

        final CompletableHandler<String> completableHandler = new CompletableHandler<>();
        vertx.deployVerticle("java-guice:" + MigrationVerticle.class.getName(), forGuiceVerticleLoader(), completableHandler.handler());

        return completableHandler;
    }


}
