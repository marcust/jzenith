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
package org.jzenith.postgresql;

import one.util.streamex.StreamEx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;

import javax.inject.Inject;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresqlHealthCheckTest extends AbstractPostgresqlPluginTest {

    private JZenith application;

    @Inject
    private Set<HealthCheck> healthChecks;

    @Before
    public void initClient() {
        application = makeApplication();
        application.run();
        application.createInjectorForTesting().injectMembers(this);
    }

    @After
    public void closeApplication() {
        if (application != null) {
            application.stop();
        }
    }

    @Test
    public void testHealthCheck() {
        final HealthCheck healthCheck = StreamEx.of(healthChecks)
                .select(HealthCheck.class)
                .findFirst()
                .orElseThrow();

        final HealthCheckResult healthCheckResult = healthCheck.execute().blockingGet();

        assertThat(healthCheckResult.isDown()).isFalse();
    }


}
