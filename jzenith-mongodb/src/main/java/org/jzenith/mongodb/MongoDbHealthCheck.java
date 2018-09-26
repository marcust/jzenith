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
package org.jzenith.mongodb;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;

import javax.inject.Inject;

public class MongoDbHealthCheck extends HealthCheck {

    private final MongoClient client;

    public static final String COMMAND_NAME = "ping";
    private final static JsonObject PING_COMMAND = new JsonObject().put(COMMAND_NAME, 1);

    @Inject
    public MongoDbHealthCheck(MongoClient client) {
        this.client = client;
    }

    @Override
    public Single<HealthCheckResult> executeInternal() {
        return client.rxRunCommand(COMMAND_NAME, PING_COMMAND)
                .map(response -> response.getDouble("ok"))
                .map(value -> createResult(value > 0.0D));
    }

}
