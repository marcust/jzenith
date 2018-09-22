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
package org.jzenith.example;

import org.junit.Test;
import org.jzenith.core.health.HealthState;
import org.jzenith.rest.health.HealthCheckResponse;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractJZenithResourcesTest {

    @Test
    public void testHealthEndpoint() {
        final HealthCheckResponse response = given()
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .extract()
                .as(HealthCheckResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getGlobalState()).isEqualTo(HealthState.UP);
    }

    @Test
    public void testMetricsEndpoint() {
        final String response = given()
                .when()
                .get("/metrics/prometheus")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThat(response).isNotBlank();
    }

    @Test
    public void testOpenApiEndpoint() {
        final String response = given()
                .when()
                .get("/openapi.json")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertThat(response).isNotNull();
    }


}
