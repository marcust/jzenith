package org.jzenith.core.health;

import io.reactivex.Single;

public interface HealthCheck {

    Single<HealthCheckResult> execute();

}
