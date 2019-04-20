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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.concurrent.atomic.AtomicReference;

public class OpenTracingInterceptor implements MethodInterceptor {

    private static final String ERROR = "error";

    private final Tracer tracer;

    public OpenTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Object returnValue = invocation.proceed();
        if (returnValue == null) {
            return null;
        }

        final String operationName = invocation.getThis().getClass().getSuperclass().getSimpleName() + "." + invocation.getMethod().getName();

        return wrapRxTypes(operationName, returnValue);
    }

    @VisibleForTesting
    Object wrapRxTypes(@NonNull String operationName, Object returnValue) {
        if (Single.class.isAssignableFrom(returnValue.getClass())) {
            return handleSingle((Single<?>) returnValue, createChildSpan(operationName));
        } else if (Observable.class.isAssignableFrom(returnValue.getClass())) {
            return handleObservable((Observable<?>) returnValue, createChildSpan(operationName));
        } else if (Completable.class.isAssignableFrom(returnValue.getClass())) {
            return handleCompletable((Completable) returnValue, createChildSpan(operationName));
        } else if (Maybe.class.isAssignableFrom(returnValue.getClass())) {
            return handleMaybe((Maybe<?>) returnValue, createChildSpan(operationName));
        }

        return returnValue;
    }

    private Single<?> handleSingle(final Single<?> single, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return single.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnSuccess(event -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(ImmutableMap.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });

    }

    private Observable<?> handleObservable(final Observable<?> observable, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return observable.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnComplete(() -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(ImmutableMap.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });
    }

    private Completable handleCompletable(final Completable completable, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return completable.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnComplete(() -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(ImmutableMap.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });
    }

    private Maybe<?> handleMaybe(final Maybe<?> maybe, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return maybe.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnComplete(() -> currentScope.get().close())
                .doOnSuccess(event -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(ImmutableMap.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });
    }

    private void activateSpan(@NonNull final AtomicReference<Scope> currentScope, @NonNull final Span span) {
        currentScope.set(tracer.scopeManager().activate(span));
    }

    @VisibleForTesting
    Span createChildSpan(@NonNull String operationName) {
        final Tracer.SpanBuilder builder = tracer.buildSpan(operationName);

        final Span span = tracer.activeSpan();
        if (span != null) {
            builder.asChildOf(span);
        }

        return builder.start();

    }

}
