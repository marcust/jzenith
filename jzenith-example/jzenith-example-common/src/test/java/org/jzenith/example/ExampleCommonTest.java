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

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import io.opentracing.noop.NoopTracerFactory;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.JZenithException;
import org.jzenith.example.mapper.MapperModule;
import org.jzenith.example.persistence.UserDao;
import org.jzenith.example.persistence.model.Deleted;
import org.jzenith.example.persistence.model.Updated;
import org.jzenith.example.resources.HelloWorldResource;
import org.jzenith.example.resources.UserResource;
import org.jzenith.example.resources.request.CreateUserRequest;
import org.jzenith.example.resources.request.UpdateUserRequest;
import org.jzenith.example.resources.response.UserResponse;
import org.jzenith.example.service.ServiceLayerModule;
import org.jzenith.example.service.model.User;
import org.jzenith.rest.model.Page;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExampleCommonTest {

    private JZenith application;

    @Inject
    private UserResource userResource;

    @Inject
    private HelloWorldResource helloWorldResource;

    @Inject
    private Vertx vertx;

    @Inject
    private UserDao userDao;

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            final UserDao userDaoMock = mock(UserDao.class);
            bind(UserDao.class).toInstance(userDaoMock);

            bind(UserResource.class);
            bind(HelloWorldResource.class);
        }
    }

    JZenith makeApplication() {
        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withModules(new ServiceLayerModule(), new MockModule(), new MapperModule());
    }

    @BeforeEach
    public void configureApplication() {
        this.application = makeApplication();
        this.application.createInjectorForTesting().injectMembers(this);
    }

    @AfterEach
    public void resetMock() {
        reset(userDao);
    }

    @Test
    public void testStartupShutdown() {
        application.run();
        application.stop();
    }

    @Test
    public void testHelloWorldResource() throws InterruptedException, ExecutionException {
        final CompletableFuture<AsyncResponse> asyncResponseFuture = CompletableFuture.supplyAsync(() -> {
            final AsyncResponse response = mock(AsyncResponse.class);
            helloWorldResource.getHelloWorld(response, vertx);
            return response;
        });

        await()
                .catchUncaughtExceptions()
                .pollDelay(500, TimeUnit.MILLISECONDS)
                .timeout(5, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        final AsyncResponse response = asyncResponseFuture.get(1, TimeUnit.SECONDS);
                        verify(response, times(1)).resume(eq("Hello World"));
                        return true;
                    } catch (final Exception e) {
                        throw new JZenithException(e);
                    }
                });
    }

    @Test
    public void testUserResourceCreate() {
        when(userDao.save(any())).thenReturn(Single.just(new User(UUID.randomUUID(), "name")));

        final CreateUserRequest createUserRequest = new CreateUserRequest("name");
        assertThat(createUserRequest).isEqualTo(createUserRequest);

        final UserResponse userResponse = userResource.createUser(createUserRequest).blockingGet();

        assertThat(userResponse.getId()).isNotNull();
        assertThat(userResponse.getName()).isEqualTo("name");
        assertThat(userResponse).isEqualTo(userResponse);
    }

    @Test
    public void testUserResourceList() {
        when(userDao.listUsers(any(), any()))
                .thenReturn(Single.just(new Page<>(0, 1, 2, ImmutableList.of(new User(UUID.randomUUID(), "name")))));

        final Page<UserResponse> listResponse = userResource.listUsers(0, 1).blockingGet();

        assertThat(listResponse.getOffset()).isEqualTo(0);
        assertThat(listResponse.getLimit()).isEqualTo(1);
        assertThat(listResponse.getTotalElements()).isEqualTo(2);
        assertThat(listResponse.getElements()).hasSize(1);
    }

    @Test
    public void testUserResourceGet() {
        when(userDao.getById(any())).thenReturn(Maybe.just(new User(UUID.randomUUID(), "name")));

        final UserResponse userResponse = userResource.getUser(UUID.randomUUID()).blockingGet();

        assertThat(userResponse.getId()).isNotNull();
        assertThat(userResponse.getName()).isEqualTo("name");
    }

    @Test
    public void testUserResourceDelete() {
        when(userDao.getById(any())).thenReturn(Maybe.just(new User(UUID.randomUUID(), "name")));
        when(userDao.deleteById(any())).thenReturn(Single.just(Deleted.YES));

        final Response deleteResponse = userResource.deleteUser(UUID.randomUUID()).blockingGet();

        assertThat(deleteResponse.getStatus()).isEqualTo(200);
    }

    @Test
    public void testUserResourceUpdate() {
        when(userDao.getById(any())).thenReturn(Maybe.just(new User(UUID.randomUUID(), "name")));
        when(userDao.updateNameById(any(), any())).thenReturn(Single.just(Updated.YES));

        final UpdateUserRequest updateUserRequest = new UpdateUserRequest("name");
        assertThat(updateUserRequest).isEqualTo(updateUserRequest);

        final UserResponse userResponse = userResource.updateUser(UUID.randomUUID(), updateUserRequest).blockingGet();

        assertThat(userResponse.getId()).isNotNull();
        assertThat(userResponse.getName()).isEqualTo("name");
    }


}


