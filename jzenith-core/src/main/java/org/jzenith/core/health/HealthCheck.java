package org.jzenith.core.health;

import io.reactivex.Single;

import java.util.concurrent.TimeUnit;

public abstract class HealthCheck {

    public Single<HealthCheckResult> execute() {
        return executeInternal().timeout(getTimeout(), getTimeoutUnit())
                .onErrorResumeNext(error -> Single.just(createResult(error)));
    }

    protected HealthCheckResult createResult(Throwable error) {
        return HealthCheckResult.create(error, getName());
    }

    protected HealthCheckResult createResult(boolean up) {
        return HealthCheckResult.create(up, getName());
    }

    protected abstract Single<HealthCheckResult> executeInternal();

    public String getName() {
        return getClass().getSimpleName();
    }

    public long getTimeout() {
        return 5;
    }

    public TimeUnit getTimeoutUnit() {
        return TimeUnit.SECONDS;
    }
}
