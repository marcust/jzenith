package org.jzenith.rest.health;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jzenith.core.health.HealthCheckResult;
import org.jzenith.core.health.HealthState;

import java.util.List;

@Getter
@Builder
public class HealthCheckResponse {

    @NonNull
    private HealthState globalState;

    @NonNull
    private List<HealthCheckResult> results;

}
