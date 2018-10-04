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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class EnvironmentVariableExpanderTest {

    @Test
    public void testPlainString() {
        final String plainString = "nothing to expand here";
        assertThat(plainString).describedAs("Should be untouched").isEqualTo(EnvironmentVariableExpander.expand(plainString));
    }

    @Test
    public void testUndefinedVariable() {
        final String variableName = "UNDEFINED";
        final String undefinedVariable = "before ${" + variableName + "} after";
        assertThrows(RuntimeException.class, () -> EnvironmentVariableExpander.expand(undefinedVariable), variableName);
    }

    @Test
    public void testDefinedVariable() {
        final String variableValue = "a value";
        final String variableName = "TEST_VARIABLE_NAME";
        final ImmutableMap<String, String> environment = ImmutableMap.of(variableName, variableValue);
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander(environment::get);

        final String expression = "prefix ${" + variableName + "} postfix";
        assertThat(expander.expandVariables(expression)).describedAs("Should contain the value now").contains(variableValue);
        assertThat(expander.expandVariables(expression)).describedAs("Should not contain the variable anymore").doesNotContain(variableName);
    }

    @Test
    public void testDefinedDefaultValueVariable() {
        final String defaultValue = "a value";
        final String variableName = "TEST_VARIABLE_NAME";
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander(Collections.<String, String>emptyMap()::get);

        final String expression = "prefix ${" + variableName + ":" + defaultValue + "} postfix";
        assertThat(expander.expandVariables(expression)).describedAs("Should contain the value now").contains(defaultValue);
        assertThat(expander.expandVariables(expression)).describedAs("Should not contain the variable anymore").doesNotContain(variableName);
    }

    @Test
    public void testNullDefaultValueVariable() {
        final String variableName = "TEST_VARIABLE_NAME";
        final String expression = "prefix ${" + variableName + ":} postfix";
        assertThrows(RuntimeException.class, () -> EnvironmentVariableExpander.expand(expression), variableName);
    }

    @Test
    public void testRecursiveValueVariable() {
        final String nestedVariableValue = "a value";
        final String nestedVariableName = "NESTED_VARIABLE_NAME";
        final String variableValue = "${" + nestedVariableName + "}";
        final String variableName = "TEST_VARIABLE_NAME";
        final ImmutableMap<String, String> environment = ImmutableMap.of(variableName, variableValue, nestedVariableName, nestedVariableValue);
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander(environment::get);

        final String expression = "prefix ${" + variableName + "} postfix";
        assertThat(expander.expandVariables(expression)).describedAs("Should contain the value now").contains(nestedVariableValue);
        assertThat(expander.expandVariables(expression)).describedAs("Should not contain the nested variable name").doesNotContain(nestedVariableName);
        assertThat(expander.expandVariables(expression)).describedAs("Should not contain the variable anymore").doesNotContain(variableName);
    }

    @Test
    public void testSpecialCharacters() {
        final String variableValue = "a value with a reference $7";
        final String variableName = "TEST_VARIABLE_NAME";
        final ImmutableMap<String, String> environment = ImmutableMap.of(variableName, variableValue);
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander(environment::get);

        final String expression = "prefix ${" + variableName + "} postfix";
        assertThat(expander.expandVariables(expression)).describedAs("Should contain the value now").contains(variableValue);
        assertThat(expander.expandVariables(expression)).describedAs("Should not contain the variable anymore").doesNotContain(variableName);
    }

    @Test
    public void testDefaultValue() {
        final String variableName = "TEST_VARIABLE_NAME:DEFAULT";
        final ImmutableMap<String, String> environment = ImmutableMap.of();
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander(environment::get);

        final String expression = "prefix ${" + variableName + "} postfix";
        assertThat(expander.expandVariables(expression)).describedAs("Should contain the value now").contains("DEFAULT");
        assertThat(expander.expandVariables(expression)).describedAs("Should not contain the variable anymore").doesNotContain(variableName);
    }

}
