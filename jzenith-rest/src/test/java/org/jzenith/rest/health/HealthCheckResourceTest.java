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
package org.jzenith.rest.health;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.opentracing.noop.NoopTracerFactory;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.JZenithException;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.health.HealthCheckResult;
import org.jzenith.core.health.HealthState;
import org.jzenith.rest.RestPlugin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckResourceTest {

    private static class FailingHealthCheckModule extends AbstractModule {

        @Override
        protected void configure() {
            final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
            healthCheckMultibinder.addBinding().toInstance(new HealthCheck() {
                @Override
                protected Single<HealthCheckResult> executeInternal() {
                    return Single.error(new JZenithException("foo"));
                }
            });
        }
    }

    @Test
    public void testFailingHealthCheck() {
        final JZenith application = JZenith.application()
                .withTracer(NoopTracerFactory.create())
                .withModules(new FailingHealthCheckModule())
                .withPlugins(RestPlugin.withResources());

        application.run();

        final HealthCheckResponse response = given()
                .when()
                .get("/health")
                .then()
                .statusCode(500)
                .extract()
                .as(HealthCheckResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getGlobalState()).isEqualTo(HealthState.DOWN);

        application.stop();
    }

}
