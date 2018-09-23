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
import io.opentracing.Tracer;
import io.opentracing.noop.NoopSpan;
import io.opentracing.noop.NoopSpanBuilder;
import io.opentracing.noop.NoopTracer;
import io.opentracing.noop.NoopTracerFactory;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.jzenith.core.JZenithException;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenTracingInterceptorTest {

    @Test
    public void testSingleInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        ((Single<?>) interceptor.wrapRxTypes("success", Single.just("success"))).blockingGet();

        try {
            ((Single<?>) interceptor.wrapRxTypes("error", Single.<String>error(new JZenithException("error")))).blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testObservableInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        ((Observable<?>) interceptor.wrapRxTypes("suceess", Observable.just("success"))).toList().blockingGet();

        try {
            ((Observable<?>) interceptor.wrapRxTypes("error", Observable.<String>error(new JZenithException("error")))).toList().blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testCompletableInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        ((Completable) interceptor.wrapRxTypes("success", Completable.complete())).blockingGet();

        try {
            ((Completable) interceptor.wrapRxTypes("error", Completable.error(new JZenithException("error")))).blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testMaybeInterceptor() {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        ((Maybe<?>) interceptor.wrapRxTypes("success", Maybe.just("success"))).blockingGet();

        try {
            ((Maybe<?>) interceptor.wrapRxTypes("error", Maybe.<String>error(new JZenithException("error")))).blockingGet();
        } catch (JZenithException e) {
            assertThat(e.getMessage()).isEqualTo("error");
        }
    }

    @Test
    public void testInvoke() throws Throwable {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);
        final MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.proceed()).thenReturn("foo");
        when(invocation.getThis()).thenReturn(this);
        when(invocation.getMethod()).thenReturn(this.getClass().getMethod("testInvoke"));

        interceptor.invoke(invocation);
    }

    @Test
    public void testInvokeNull() throws Throwable {
        final NoopTracer tracer = NoopTracerFactory.create();
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);
        final MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.proceed()).thenReturn(null);

        interceptor.invoke(invocation);
    }

    @Test
    public void testCreateChildSpan() throws Throwable {
        final Tracer tracer = mock(Tracer.class);
        final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

        when(tracer.buildSpan(any())).thenReturn(NoopSpanBuilder.INSTANCE);
        when(tracer.activeSpan()).thenReturn(mock(Span.class));

        final Span span = interceptor.createChildSpan("foo");
        assertThat(span).isEqualTo(NoopSpan.INSTANCE);
    }


}