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
package org.jzenith.rest.tracing;


import io.opentracing.Scope;
import io.opentracing.Span;
import lombok.NonNull;

public class RequestScopedScope implements Scope {

    private final RequestScopedScopeManager scopeManager;
    private final boolean finishOnClose;
    private final Span wrapped;
    private final RequestScopedScope toRestore;

    public RequestScopedScope(@NonNull final RequestScopedScopeManager scopeManager,
                              final boolean finishOnClose,
                              @NonNull final Span wrapped,
                              final RequestScopedScope toRestore) {
        this.scopeManager = scopeManager;
        this.finishOnClose = finishOnClose;
        this.wrapped = wrapped;
        this.toRestore = toRestore;
        scopeManager.setScope(this);
    }

    @Override
    public void close() {
        if (scopeManager.getScope() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }

        if (finishOnClose) {
            wrapped.finish();
        }

        scopeManager.setScope(toRestore);
    }

    @Override
    @Deprecated
    public Span span() {
        return wrapped;
    }

    Span activeSpan() {
        return wrapped;
    }
}
