package org.jzenith.core.health;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Builder
public class HealthCheckResult {

    @NonNull
    private final String healtCheckName;

    @NonNull
    private final HealthState state;

    private final String message;

    public static HealthCheckResult create(boolean up, @NonNull String name) {
        return HealthCheckResult.builder().state(up ? HealthState.UP : HealthState.DOWN).healtCheckName(name).build();
    }

    public static HealthCheckResult create(@NonNull Throwable error, @NonNull String name) {
        return HealthCheckResult.builder().state(HealthState.DOWN).message(error.getMessage()).healtCheckName(name).build();
    }

    @JsonIgnore
    public boolean isDown() {
        return this.state == HealthState.DOWN;
    }
}
