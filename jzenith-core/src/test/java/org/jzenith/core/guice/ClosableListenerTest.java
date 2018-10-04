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
package org.jzenith.core.guice;

import com.google.inject.Binding;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.ProvisionListener;
import org.junit.jupiter.api.Test;

import java.io.Closeable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClosableListenerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testClosableListenerClosable() {
        final CloseableListener listener = new CloseableListener(new LifeCycleObjectRepository());
        final ProvisionListener.ProvisionInvocation invocation = mockInvocation();
        when(invocation.provision()).thenReturn(mock(Closeable.class));

        listener.onProvision(invocation);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testClosableListenerAutoClosable() {
        final CloseableListener listener = new CloseableListener(new LifeCycleObjectRepository());
        final ProvisionListener.ProvisionInvocation invocation = mockInvocation();
        when(invocation.provision()).thenReturn(mock(AutoCloseable.class));

        listener.onProvision(invocation);

    }

    @SuppressWarnings("unchecked")
    private ProvisionListener.ProvisionInvocation<?> mockInvocation() {
        final ProvisionListener.ProvisionInvocation invocation = mock(ProvisionListener.ProvisionInvocation.class);

        final Binding binding = mock(Binding.class);

        when(invocation.getBinding()).thenReturn(binding);
        when(binding.acceptScopingVisitor(any())).thenAnswer(value -> {
            final BindingScopingVisitor visitor = value.getArgument(0);
            visitor.visitEagerSingleton();
            visitor.visitNoScoping();
            visitor.visitScope(Scopes.SINGLETON);
            visitor.visitScopeAnnotation(Singleton.class);
            return Boolean.TRUE;
        });
        return invocation;
    }

}
