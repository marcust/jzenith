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
package org.jzenith.redis;

import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;

import javax.inject.Inject;

public class RedisHealthCheck extends HealthCheck {

    private final RedisClient client;

    @Inject
    public RedisHealthCheck(RedisClient client) {
        this.client = client;
    }

    @Override
    public Single<HealthCheckResult> executeInternal() {
        return client.rxPing()
                .map(response -> createResult("PONG".equalsIgnoreCase(response)));
    }


}