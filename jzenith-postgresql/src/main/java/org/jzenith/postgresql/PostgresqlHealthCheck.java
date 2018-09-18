/**
 * Copyright (C) 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
