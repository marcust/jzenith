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

import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.internal.observers.FutureSingleObserver;
import io.reactivex.internal.schedulers.NewThreadScheduler;
import org.junit.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.JZenithException;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckTest {

    private static class TestHealthCheck extends HealthCheck {

        @Override
        protected Single<HealthCheckResult> executeInternal() {
            return Single.just(createResult(true));
        }
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            final Multibinder<HealthCheck> healthCheckMultibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
            healthCheckMultibinder.addBinding().to(TestHealthCheck.class);
        }
    }

    @Inject
    private Set<HealthCheck> healthChecks;

    @Test
    public void testHealthCheck() {
        final Injector injector = JZenith.application()
                .withModules(new HealthCheckTest.TestModule())
                .createInjectorForTesting();

        injector.injectMembers(this);

        assertThat(healthChecks).isNotNull();
        assertThat(healthChecks).hasSize(1);

        final HealthCheck onlyElement = Iterables.getOnlyElement(healthChecks);
        assertThat(onlyElement.getName()).isEqualTo("TestHealthCheck");
        assertThat(onlyElement.getTimeout()).isEqualTo(Duration.ofSeconds(5));
        final HealthCheckResult result = onlyElement.execute().blockingGet();
        assertThat(result.isDown()).isFalse();
        assertThat(result.getHealtCheckName()).isEqualTo("TestHealthCheck");
        assertThat(result.getMessage()).isNull();
        assertThat(result.getState()).isEqualTo(HealthState.UP);
    }

    @Test
    public void testFailedHealthCheck() {
        final HealthCheck failedHealthCheck = new HealthCheck() {
            @Override
            protected Single<HealthCheckResult> executeInternal() {
                return Single.error(new JZenithException("Test"));
            }
        };

        final HealthCheckResult result = failedHealthCheck.execute().blockingGet();
        assertThat(result.isDown()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Test");
        assertThat(result.getState()).isEqualTo(HealthState.DOWN);
    }

    @Test
    public void testTimedOutHealthCheck() throws ExecutionException, InterruptedException {
        final HealthCheck timedoutHealthCheck = new HealthCheck() {
            @Override
            protected Single<HealthCheckResult> executeInternal() {
                return Observable.interval(getTimeout().toMillis() * 2, TimeUnit.MILLISECONDS)
                        .flatMap(value -> Observable.just(createResult(new JZenithException("Should not come here"))))
                        .firstOrError();
            }
        };

        final FutureSingleObserver<HealthCheckResult> subscriber = new FutureSingleObserver<>();
        timedoutHealthCheck.execute()
                .subscribeOn(new NewThreadScheduler())
                .observeOn(new NewThreadScheduler())
                .subscribe(subscriber);

        final HealthCheckResult result = subscriber.get();

        assertThat(result.isDown()).isTrue();
        assertThat(result.getMessage()).isEqualTo("TimeoutException");
        assertThat(result.getState()).isEqualTo(HealthState.DOWN);
    }



}
