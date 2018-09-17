package org.jzenith.core.health;

import lombok.*;

@Getter
@Builder
public class HealthCheckResult {

    @NonNull
    private final String healtCheckName;

    @NonNull
    private final HealthState state;

    public static HealthCheckResult create(boolean up, @NonNull String name) {
        return HealthCheckResult.builder().state(up ? HealthState.UP : HealthState.DOWN).healtCheckName(name).build();
    }

    public boolean isDown() {
        return this.state == HealthState.DOWN;
    }
}
