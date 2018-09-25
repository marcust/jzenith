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

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jzenith.core.CoreConfiguration;
import org.jzenith.core.JZenithException;
import org.jzenith.core.util.EnvironmentVariableExpander;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Properties;
import java.util.function.Supplier;

@Slf4j
public class ConfigurationProvider<T> implements Provider<T> {

    private final Class<T> configurationClass;
    private final String configurationBaseNameUpper;

    @Inject
    private CoreConfiguration coreConfiguration;

    @Inject
    private ExtraConfiguration extraConfiguration;

    public ConfigurationProvider(Class<T> configurationClass) {
        this.configurationClass = configurationClass;
        this.configurationBaseNameUpper = Splitter.on('_').split(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, configurationClass.getSimpleName())).iterator().next();
    }

    @Override
    public T get() {
        return configurationClass.cast(Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{configurationClass},
                new ConfigurationInvocationHandler(configurationBaseNameUpper, coreConfiguration, extraConfiguration)));
    }

    private static class ConfigurationInvocationHandler implements InvocationHandler {

        private final String configurationBaseNameUpper;
        private final CoreConfiguration coreConfiguration;
        private final ExtraConfiguration extraConfiguration;

        private ConfigurationInvocationHandler(String configurationBaseNameUpper,
                                               CoreConfiguration coreConfiguration,
                                               ExtraConfiguration extraConfiguration) {
            this.configurationBaseNameUpper = configurationBaseNameUpper;
            this.coreConfiguration = coreConfiguration;
            this.extraConfiguration = extraConfiguration;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final String configurationName = method.getName().replace("get", "");
            final String environmentVariableName = configurationBaseNameUpper + "_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, configurationName);
            final String configurationNameCommandLine = "--" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, environmentVariableName);
            final String configurationPropertyName = configurationBaseNameUpper.toLowerCase() + "." + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, configurationName).replace('_', '.');

            final Object value = firstNonNull(
                    () -> logValue(extraConfigurationValue(configurationPropertyName), configurationPropertyName, configurationPropertyName, "code"),
                    () -> logValue(commandLineArgument(configurationNameCommandLine), configurationPropertyName, configurationNameCommandLine, "command line"),
                    () -> logValue(environmentConfiguration(environmentVariableName), configurationPropertyName, environmentVariableName, "environment"),
                    () -> logValue(propertyConfiguration(configurationBaseNameUpper.toLowerCase(), configurationPropertyName), configurationPropertyName, configurationPropertyName, "property file"),
                    () -> logValue(defaultConfiguration(method), configurationPropertyName, method.getName(), "default annotation"));

            final Class<?> returnType = method.getReturnType();
            if (value.getClass() == returnType) {
                return value;
            }

            if (value.getClass() == Integer.class && returnType == int.class) {
                return value;
            }

            if (value.getClass() == Long.class && returnType == long.class) {
                return value;
            }


            if (value instanceof String) {
                return handleVariableExpansion((String) value, returnType);
            }

            throw new IllegalStateException("Don't know how to handle a value of type " + value.getClass() + "(" + value + ") in " + method.getReturnType() + " context");
        }

        private Object logValue(Object value, String propertyName, String lookupName, String from) {
            if (value != null) {
                log.info("Using value '" + value + "' for property " + propertyName + " obtained from " + from + " (" + lookupName + ")");
            }
            return value;
        }

        private Object extraConfigurationValue(String key) {
            return extraConfiguration.getValue(key);
        }

        private String defaultConfiguration(Method method) {
            final ConfigDefault annotation = method.getAnnotation(ConfigDefault.class);
            if (annotation == null) {
                throw new IllegalStateException("Method " + method.getName() + " of type " + method.getDeclaringClass().getName() + " does not have a @ConfigDefault annotation");
            }

            return annotation.value();
        }

        @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION", justification = "Wrong positive on unclosed stream")
        @SneakyThrows
        private String propertyConfiguration(String configurationBaseNameLower, String propertyName) {
            try (final InputStream specificStream = this.getClass().getResourceAsStream("/" + configurationBaseNameLower + ".properties")) {
                if (specificStream != null) {
                    final String value = loadPropertyFrom(specificStream, propertyName);
                    if (value != null) {
                        return value;
                    }
                }
            }

            try (final InputStream globalStream = this.getClass().getResourceAsStream("/jzenith.properties")) {
                if (globalStream != null) {
                    return loadPropertyFrom(globalStream, propertyName);
                }
            }

            return null;
        }

        @SneakyThrows
        private String loadPropertyFrom(InputStream inputStream, String propertyName) {
            final Properties p = new Properties();
            p.load(inputStream);

            if (p.containsKey(propertyName)) {
                return p.getProperty(propertyName);
            }

            return null;
        }

        private String environmentConfiguration(String environmentVariableName) {
            return System.getenv(environmentVariableName);
        }

        private String commandLineArgument(String configurationName) {
            final Iterator<String> iterator = coreConfiguration.getCommandLineArguments().iterator();
            while (iterator.hasNext()) {
                if (configurationName.equals(iterator.next())) {
                    if (!iterator.hasNext()) {
                        throw new IllegalStateException("Command line argument " + configurationName + " should have a value");
                    }
                    return iterator.next();
                }
            }
            return null;
        }

        @SafeVarargs
        private static Object firstNonNull(Supplier<Object>... suppliers) {
            for (final Supplier<Object> supplier : suppliers) {
                final Object s = supplier.get();
                if (s != null) {
                    return s;
                }
            }

            throw new IllegalStateException("Non of the provided suppliers supplied a value");
        }

        private static Object handleVariableExpansion(@NonNull String stringValue, @NonNull Class<?> returnType) {
            final String expandedValue;
            if (stringValue.startsWith("$")) {
                expandedValue = EnvironmentVariableExpander.expand(stringValue);
            } else if (stringValue.startsWith("\\")) {
                expandedValue = stringValue.substring(1);
            } else {
                expandedValue = stringValue;
            }

            if (returnType == int.class) {
                return Integer.parseInt(expandedValue);
            }
            if (returnType == String.class) {
                return expandedValue;
            }
            throw new NotImplementedException("No support for configuration of type " + returnType.getName());
        }

    }
}