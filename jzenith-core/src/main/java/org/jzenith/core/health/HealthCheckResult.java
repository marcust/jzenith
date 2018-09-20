/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.core.health;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthCheckResult {

    @NonNull
    private String healtCheckName;

    @NonNull
    private HealthState state;

    private String message;

    public static HealthCheckResult create(boolean up, @NonNull String name) {
        return HealthCheckResult.builder().state(up ? HealthState.UP : HealthState.DOWN).healtCheckName(name).build();
    }

    public static HealthCheckResult create(@NonNull Throwable error, @NonNull String name) {
        return HealthCheckResult.builder().state(HealthState.DOWN).message(MoreObjects.firstNonNull(error.getMessage(), error.getClass().getSimpleName())).healtCheckName(name).build();
    }

    @JsonIgnore
    public boolean isDown() {
        return this.state == HealthState.DOWN;
    }
}
