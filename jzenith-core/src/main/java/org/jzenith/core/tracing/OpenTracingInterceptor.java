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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class OpenTracingInterceptor implements MethodInterceptor {

    private static final String ERROR = "error";

    private final Tracer tracer;

    public OpenTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Class<?> returnType = invocation.getMethod().getReturnType();

        if (returnType == Single.class) {
            return handleSingle((Single<?>) invocation.proceed(), createChildSpan(invocation));
        } else if (returnType == Observable.class) {
            return handleObservable((Observable<?>)invocation.proceed(), createChildSpan(invocation));
        } else if (returnType == Completable.class) {
            return handleCompletable((Completable)invocation.proceed(), createChildSpan(invocation));
        } else if (returnType == Maybe.class) {
            return handleMaybe((Maybe<?>)invocation.proceed(), createChildSpan(invocation));
        }

        return invocation.proceed();
    }

    @VisibleForTesting
    Single<?> handleSingle(final Single<?> single, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return single.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnSuccess(event -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(Map.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });

    }

    @VisibleForTesting
    Observable<?> handleObservable(final Observable<?> observable, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return observable.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnComplete(() -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(Map.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });
    }

    @VisibleForTesting
    Completable handleCompletable(final Completable completable, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return completable.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnComplete(() -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(Map.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });
    }

    @VisibleForTesting
    Maybe<?> handleMaybe(final Maybe<?> maybe, final Span span) {
        final AtomicReference<Scope> currentScope = new AtomicReference<>();

        return maybe.doOnSubscribe(disposable -> activateSpan(currentScope, span))
                .doOnComplete(() -> currentScope.get().close())
                .doOnSuccess(event -> currentScope.get().close())
                .doOnError(ex -> {
                    Tags.ERROR.set(span, true);
                    span.log(Map.of(Fields.EVENT, ERROR, Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
                    currentScope.get().close();
                });
    }

    private void activateSpan(@NonNull final AtomicReference<Scope> currentScope, @NonNull final Span span) {
        currentScope.set(tracer.scopeManager().activate(span, true));
    }

    private Span createChildSpan(@NonNull final MethodInvocation invocation) {
        final Tracer.SpanBuilder builder = tracer.buildSpan(invocation.getThis().getClass().getSuperclass().getSimpleName() + "." + invocation.getMethod().getName());

        final Span span = tracer.activeSpan();
        if (span != null) {
            builder.asChildOf(span);
        }

        return builder.start();

    }

}
