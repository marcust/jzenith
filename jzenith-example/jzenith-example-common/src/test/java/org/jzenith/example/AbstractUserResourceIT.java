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

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.jzenith.example.persistence.UserDao;
import org.jzenith.example.resources.request.CreateUserRequest;
import org.jzenith.example.resources.request.UpdateUserRequest;
import org.jzenith.example.resources.response.UserResponse;
import org.jzenith.example.service.model.User;
import org.jzenith.rest.model.ErrorResponse;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractUserResourceIT {

    protected final static UUID USER_UUID = UUID.fromString("71aebc48-78e2-4298-8d13-7a2bfb58c555");
    protected final static String USER_NAME = "jzenith_user";

    protected static Injector injector;

    @Inject
    private UserDao userDao;

    @Before
    public void setup() throws Exception {
        injector.injectMembers(this);
    }

    @Test
    public void testGetUser_200() {
        final UserResponse response = given()
                .when()
                .get("/user/{id}", USER_UUID)
                .then()
                .statusCode(200)
                .extract()
                .as(UserResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(USER_UUID);
        assertThat(response.getName()).isEqualTo(USER_NAME);
    }

    @Test
    public void testGetUser_404() {
        final ErrorResponse response = given()
                .when()
                .get("/user/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetUser_400() {
        final ErrorResponse response = given()
                .when()
                .get("/user/{id}", "foo")
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testDeleteUser_200() {
        final String response = given()
                .when()
                .delete("/user/{id}", USER_UUID)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertThat(response).isBlank();

        final User dbUser = userDao.getById(USER_UUID).blockingGet();
        assertThat(dbUser).isNull();
    }

    @Test
    public void testDeleteUser_404() {
        final ErrorResponse response = given()
                .when()
                .delete("/user/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteUser_400() {
        final ErrorResponse response = given()
                .when()
                .delete("/user/{id}", "foo")
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testUpdateUser_200() {
        final UserResponse response = given()
                .body(new UpdateUserRequest("new_user_name"))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .when()
                .put("/user/{id}", USER_UUID)
                .then()
                .statusCode(200)
                .extract()
                .as(UserResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(USER_UUID);
        assertThat(response.getName()).isEqualTo("new_user_name");

        final User dbUser = userDao.getById(USER_UUID).blockingGet();
        assertThat(dbUser).isNotNull();
        assertThat(dbUser.getId()).isEqualTo(USER_UUID);
        assertThat(dbUser.getName()).isEqualTo("new_user_name");
    }

    @Test
    public void testUpdateUser_404() {
        final ErrorResponse response = given()
                .body(new UpdateUserRequest("new_user_name"))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .when()
                .put("/user/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testUpdateUser_400() {
        final ErrorResponse response = given()
                .body(new UpdateUserRequest("new_user_name"))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .when()
                .put("/user/{id}", "foo")
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testCreateUser_200() {
        final UserResponse response = given()
                .body(new CreateUserRequest("new_user_name"))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .when()
                .post("/user")
                .then()
                .statusCode(200)
                .extract()
                .as(UserResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("new_user_name");

        final User dbUser = userDao.getById(response.getId()).blockingGet();
        assertThat(dbUser).isNotNull();
        assertThat(dbUser.getName()).isEqualTo("new_user_name");
    }

    @Test
    public void testCreateUser_400() {
        final ErrorResponse response = given()
                .body(new CreateUserRequest(RandomStringUtils.randomAlphabetic(205)))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .when()
                .post("/user")
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testListUser_200() {
        final Page<UserResponse> response = given()
                .when()
                .get("/user")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeLiteral<Page<UserResponse>>() {}.getType());

        assertThat(response).isNotNull();
        assertThat(response.getOffset()).isEqualTo(0);
        assertThat(response.getLimit()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getElements()).isNotNull();
        assertThat(response.getElements().size()).isEqualTo(2);
        assertThat(response.getElements().get(0).getId()).isEqualTo(USER_UUID);
        assertThat(response.getElements().get(0).getName()).isEqualTo(USER_NAME);
    }

    @Test
    public void testListUser_400() {
        final ErrorResponse response = given()
                .when()
                .get("/user?limit=300")
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
