package org.jzenith.postgresql;

import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactivex.Single;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;

import javax.inject.Inject;

public class PostgresqlHealthCheck implements HealthCheck {

    private final PgPool pool;

    @Inject
    public PostgresqlHealthCheck(PgPool pool) {
        this.pool = pool;
    }

    @Override
    public Single<HealthCheckResult> execute() {
        return pool.rxQuery("select 1")
                .map(pgRowSet -> HealthCheckResult.create(pgRowSet.size() > 0, PostgresqlHealthCheck.class.getSimpleName()))
                .onErrorResumeNext(error -> Single.just(HealthCheckResult.create(error, PostgresqlHealthCheck.class.getSimpleName())));
    }
}
