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
package org.jzenith.core.tracing;

import io.opentracing.Span;
import io.opentracing.noop.NoopTracer;
import io.opentracing.noop.NoopTracerFactory;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;
import org.jzenith.core.JZenithException;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenTracingInterceptorTest {

    @Test
    public void testSingleInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        final Span success = tracer.buildSpan("success").start();
        interceptor.handleSingle(Single.just("success"), success).blockingGet();

        final Span error = tracer.buildSpan("error").start();
        try {
            interceptor.handleSingle(Single.<String>error(new JZenithException("error")), error).blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testObservableInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        final Span success = tracer.buildSpan("success").start();
        interceptor.handleObservable(Observable.just("success"), success).toList().blockingGet();

        final Span error = tracer.buildSpan("error").start();
        try {
            interceptor.handleObservable(Observable.<String>error(new JZenithException("error")), error).toList().blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testCompletableInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        final Span success = tracer.buildSpan("success").start();
        interceptor.handleCompletable(Completable.complete(), success).blockingGet();

        final Span error = tracer.buildSpan("error").start();
        try {
            interceptor.handleCompletable(Completable.error(new JZenithException("error")), error).blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testMaybeInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        final Span success = tracer.buildSpan("success").start();
        interceptor.handleMaybe(Maybe.just("success"), success).blockingGet();

        final Span error = tracer.buildSpan("error").start();
        try {
            interceptor.handleMaybe(Maybe.<String>error(new JZenithException("error")), error).blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }




}

