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
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class RequestScopedScopeManager implements ScopeManager {

    @Override
    public Scope activate(Span span) {
        return new RequestScopedScope(this, false, span, getScope());
    }

    @Override
    public Span activeSpan() {
        final RequestScopedScope scope = getScope();
        if (scope != null) {
            return scope.activeSpan();
        }

        return null;
    }

    public RequestScopedScope getScope() {
        return ResteasyProviderFactory.getContextData(RequestScopedScope.class);
    }

    public void setScope(RequestScopedScope requestScopedScope) {
        ResteasyProviderFactory.pushContext(RequestScopedScope.class, requestScopedScope);
    }
}
