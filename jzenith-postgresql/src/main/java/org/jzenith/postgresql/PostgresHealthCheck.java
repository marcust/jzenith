package org.jzenith.postgresql;

import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactivex.Single;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;

import javax.inject.Inject;

public class PostgresHealthCheck implements HealthCheck {

    private final PgPool pool;

    @Inject
    public PostgresHealthCheck(PgPool pool) {
        this.pool = pool;
    }

    @Override
    public Single<HealthCheckResult> execute() {
        return pool.rxQuery("select 1").map(pgRowSet -> HealthCheckResult.create(pgRowSet.size() > 0, PostgresHealthCheck.class.getSimpleName()));
    }
}
