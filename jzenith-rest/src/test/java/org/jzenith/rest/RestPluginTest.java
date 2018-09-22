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
package org.jzenith.rest;

import com.google.inject.TypeLiteral;
import io.opentracing.noop.NoopTracerFactory;
import org.junit.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.JZenithException;
import org.jzenith.core.health.HealthState;
import org.jzenith.rest.health.HealthCheckResponse;
import org.jzenith.rest.model.ErrorResponse;
import org.jzenith.rest.model.Page;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RestPluginTest {

    @Test
    public void testStartupShutdown() {
        final JZenith application = makeApplication();
        application.run();
        application.stop();
    }

    @Test
    public void testOpenApiResource() {
        final JZenith application = makeApplication();
        application.run();

        final String response = given()
                .when()
                .get("/openapi.json")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertThat(response).isNotNull();

        application.stop();
    }

    @Test
    public void testHealthCheckResource() {
        final JZenith application = makeApplication();
        application.run();

        final HealthCheckResponse response = given()
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .extract()
                .as(HealthCheckResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getGlobalState()).isEqualTo(HealthState.UP);

        application.stop();
    }

    @Test
    public void testPrometheusResource() {
        final JZenith application = makeApplication();
        application.run();

        final String response = given()
                .when()
                .get("/metrics/prometheus")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThat(response).isNotBlank();

        application.stop();
    }

    @Test
    public void testErrorHandlingMappingDynamic() {
        final JZenith application = JZenith.application();
        application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(RestPlugin.withResources(TestResource.class).withMapping(JZenithException.class, 404))
                .run();

        final ErrorResponse response = given()
                .when()
                .get("/error")
                .then()
                .statusCode(404)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("error");

        application.stop();
    }

    @Test
    public void testErrorHandlingMappingStatic() {
        final JZenith application = JZenith.application();
        application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(RestPlugin.withResources(TestResource.class)
                        .withMapping(JZenithException.class, 404, "static error message"))
                .run();

        final ErrorResponse response = given()
                .when()
                .get("/error")
                .then()
                .statusCode(404)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("static error message");

        application.stop();
    }


    @Test
    public void testPage() {
        final JZenith application = JZenith.application();
        application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(RestPlugin.withResources(TestResource.class))
                .run();

        final Page<UUID> response = given()
                .when()
                .get("/page")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeLiteral<Page<UUID>>() {}.getType());

        assertThat(response).isNotNull();
        assertThat(response.getOffset()).isEqualTo(0);
        assertThat(response.getLimit()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getElements()).isNotNull();
        assertThat(response.getElements()).hasSize(1);

        application.stop();
    }

    private JZenith makeApplication(Class<?>... resources) {
        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(RestPlugin.withResources(resources));
    }



}
