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
package org.jzenith.core.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenith;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationTest {

    interface TestConfiguration {
        int getInt();

        String getString();

        UUID getUuid();

        @ConfigDefault("default")
        String getDefault();

        String getCommandLine();

        String getLocalPropertiesValue();

        String getGlobalPropertiesValue();

        int getIntForInteger();

        long getLongForLong();

        String getFailNoValue();

        @ConfigDefault("${FOO:BAR}")
        String getWithEnvironmentDefault();

        @ConfigDefault("\\${LITERAL}")
        String getEscapedVariable();

        @ConfigDefault("foo")
        Thread getUnhandledType();

        @ConfigDefault("foo, bar")
        List<String> getList();
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(TestConfiguration.class).toProvider(new ConfigurationProvider<>(TestConfiguration.class));
        }
    }

    @Test
    public void testConfigurationBinding() {
        final UUID uuid = UUID.randomUUID();
        final Injector injector = JZenith.application("--testCommandLine", "commandLineValue")
                .withModules(new TestModule())
                .withConfiguration("test.int", 5)
                .withConfiguration("test.string", "foo")
                .withConfiguration("test.uuid", uuid)
                .withConfiguration("test.int.for.integer", Integer.valueOf(4))
                .withConfiguration("test.long.for.long", Long.valueOf(6))
                .createInjectorForTesting();

        final TestConfiguration instance = injector.getInstance(TestConfiguration.class);
        assertThat(instance).isNotNull();
        assertThat(instance.getInt()).isEqualTo(5);
        assertThat(instance.getString()).isEqualTo("foo");
        assertThat(instance.getUuid()).isEqualTo(uuid);
        assertThat(instance.getDefault()).isEqualTo("default");
        assertThat(instance.getCommandLine()).isEqualTo("commandLineValue");
        assertThat(instance.getLocalPropertiesValue()).isEqualTo("localPropertiesValue");
        assertThat(instance.getGlobalPropertiesValue()).isEqualTo("globalPropertiesValue");
        assertThat(instance.getIntForInteger()).isEqualTo(4);
        assertThat(instance.getLongForLong()).isEqualTo(6);
        assertThat(instance.getList()).containsAll(ImmutableList.of("foo", "bar"));
    }

    @Test
    public void testEnvironmentVariables() {
        final Injector injector = JZenith.application()
                .withModules(new TestModule()).createInjectorForTesting();

        final TestConfiguration instance = injector.getInstance(TestConfiguration.class);

        assertThat(instance.getWithEnvironmentDefault()).isEqualTo("BAR");
        assertThat(instance.getEscapedVariable()).isEqualTo("${LITERAL}");
    }

    @Test
    public void testFailNoValue() {
        assertThrows(IllegalStateException.class, () -> {
            final Injector injector = JZenith.application()
                    .withModules(new TestModule()).createInjectorForTesting();

            final TestConfiguration instance = injector.getInstance(TestConfiguration.class);

            instance.getFailNoValue();
        });
    }

    @Test
    public void testCommandLineError() {
        assertThrows(IllegalStateException.class, () -> {
            final Injector injector = JZenith.application("--testCommandLine")
                    .withModules(new TestModule()).createInjectorForTesting();

            final TestConfiguration instance = injector.getInstance(TestConfiguration.class);

            instance.getCommandLine();
        });
    }

    @Test
    public void testUnhandledType() {
        assertThrows(NotImplementedException.class, () -> {
            final Injector injector = JZenith.application()
                    .withModules(new TestModule()).createInjectorForTesting();

            final TestConfiguration instance = injector.getInstance(TestConfiguration.class);

            instance.getUnhandledType();
        });
    }

    @Test
    public void testFirstNonNullFail() {
        assertThrows(IllegalStateException.class, () ->
                ConfigurationProvider.firstNonNull(() -> null));
    }

}
