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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RequestScopedScopeManagerTest {

    @Test
    public void testScopeManager() {
        final RequestScopedScopeManager manager = new RequestScopedScopeManager();

        final Span mockSpan = mock(Span.class);
        final Scope active = manager.activate(mockSpan, true);

        final RequestScopedScope scope = manager.getScope();
        assertThat(scope).isEqualTo(manager.active());
        assertThat(active).isEqualTo(scope);

        assertThat(scope.span()).isEqualTo(mockSpan);
        scope.close();
    }

    @Test
    public void testScopeManagerWrongScope() {
        final RequestScopedScopeManager manager = new RequestScopedScopeManager();

        final Span mockSpan = mock(Span.class);
        final Scope active = manager.activate(mockSpan, true);

        final RequestScopedScope scope = manager.getScope();
        assertThat(scope).isEqualTo(manager.active());
        assertThat(active).isEqualTo(scope);

        assertThat(scope.span()).isEqualTo(mockSpan);

        manager.activate(mockSpan, false);
        scope.close();
    }

}
