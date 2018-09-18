package org.jzenith.postgresql;

import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactivex.Single;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;

import javax.inject.Inject;

public class PostgresqlHealthCheck extends HealthCheck {

    private final PgPool pool;

    @Inject
    public PostgresqlHealthCheck(PgPool pool) {
        this.pool = pool;
    }

    @Override
    public Single<HealthCheckResult> executeInternal() {
        return pool.rxQuery("select 1")
                .map(pgRowSet -> createResult(pgRowSet.size() > 0))
                .onErrorResumeNext(error -> Single.just(createResult(error)));
    }


}
