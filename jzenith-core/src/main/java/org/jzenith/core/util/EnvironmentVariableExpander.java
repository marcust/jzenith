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
package org.jzenith.core.util;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class EnvironmentVariableExpander {

    private static final EnvironmentVariableExpander INSTANCE = new EnvironmentVariableExpander();

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([\\w.-]+)(?:\\:([^\\}]+)?)?\\}");

    private final Function<String, String> variableAccessor;

    @VisibleForTesting
    EnvironmentVariableExpander(final Function<String, String> variableResolver) {
        this.variableAccessor = variableResolver;
    }

    private EnvironmentVariableExpander() {
        this(System::getenv);
    }

    public static String expand(final String input) {
        return INSTANCE.expandVariables(input);
    }

    @VisibleForTesting
    String expandVariables(@Nullable final String input) {
        if (input == null) {
            return null;
        }

        final Matcher m = VARIABLE_PATTERN.matcher(input);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final String variableName = m.group(1);
            final String defaultValue = m.group(2);
            final String variableValue = Optional
                    .ofNullable(expandVariables(variableAccessor.apply(variableName)))
                    .orElse(defaultValue);

            if (variableValue == null) {
                throw new IllegalStateException("Can not get value for variable " + variableName + " from current environment");
            } else {
                log.info("Expanding placeholder \"{}\" with value \"{}\"", variableName, variableValue);
            }

            m.appendReplacement(sb, Matcher.quoteReplacement(variableValue));
        }

        m.appendTail(sb);
        return sb.toString();
    }

}
