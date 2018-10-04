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
package org.jzenith.core;

import com.google.inject.Module;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.junit.jupiter.api.Test;
import org.jzenith.core.util.TestUtil;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JZenithTest {

    @Test
    public void testStartupAndShutdownBare() {
        final JZenith application = JZenith.application();
        application
                .run();
        application.stop();
    }

    @Test
    public void testPublicMethodsNonNull() {
        TestUtil.testApiMethodsHaveNonNullParameters(JZenith.application());
    }

    @Test
    public void testMissingPlugins() {
        assertThrows(IllegalArgumentException.class, JZenith.application()::withPlugins);
    }

    @Test
    public void testStartupAndShutdownPlugin() {
        final AbstractPlugin plugin = mockPlugin(CompletableFuture.completedFuture("Test"));

        final JZenith application = JZenith.application();
        application
                .withPlugins(plugin)
                .run();


        application.stop();

        verify(plugin, times(1)).getModules();
        verify(plugin, times(1)).getExtraConfiguration();
    }

    @Test
    public void testStartupAndShutdownError() {
        assertThrows(JZenithException.class, () -> {
            final CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new JZenithException("Something went wrong"));
            final AbstractPlugin plugin = mockPlugin(future);

            final JZenith application = JZenith.application();
            try {
                application
                        .withPlugins(plugin)
                        .run();
            } finally {
                application.stop();
            }
        });
    }

    @Test
    public void testStartupWithTracer() {
        final Tracer tracer = Mockito.mock(Tracer.class);

        final JZenith application = JZenith.application()
                .withTracer(tracer);

        application.run();
        application.stop();

        assertThat(GlobalTracer.isRegistered()).isTrue();
    }

    @Test
    public void testMethodCoverage() {
        JZenith.application()
                .withModules(mock(Module.class));
    }

    private AbstractPlugin mockPlugin(CompletableFuture<String> completableFuture) {
        final AbstractPlugin plugin = mock(AbstractPlugin.class);
        when(plugin.getExtraConfiguration()).thenCallRealMethod();
        when(plugin.getModules()).thenCallRealMethod();

        when(plugin.start(any())).thenReturn(completableFuture);
        return plugin;
    }

}
