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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jzenith.core.util.EnvironmentVariableExpander;

import java.util.Collections;

import static org.junit.Assert.*;

public class EnvironmentVariableExpanderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPlainString() {
        final String plainString = "nothing to expand here";
        assertEquals("Should be untouched", plainString, EnvironmentVariableExpander.expand(plainString));
    }

    @Test
    public void testUndefinedVariable() {
        final String variableName = "UNDEFINED";
        final String undefinedVariable = "before ${" + variableName + "} after";
        thrown.expect( RuntimeException.class );
        thrown.expectMessage( variableName );
        EnvironmentVariableExpander.expand(undefinedVariable);
    }

    @Test
    public void testDefinedVariable() {
        final String variableValue = "a value";
        final String variableName = "TEST_VARIABLE_NAME";
        final ImmutableMap<String,String> environment = ImmutableMap.of( variableName, variableValue );
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander( environment::get );

        final String expression = "prefix ${" + variableName + "} postfix";
        assertTrue("Should contain the value now", expander.expandVariables( expression ).contains( variableValue ) );
        assertFalse("Should not contain the variable anymore", expander.expandVariables( expression ).contains( variableName ) );
    }

    @Test
    public void testDefinedDefaultValueVariable() {
        final String defaultValue = "a value";
        final String variableName = "TEST_VARIABLE_NAME";
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander(Collections.<String, String>emptyMap()::get);

        final String expression = "prefix ${" + variableName + ":" + defaultValue + "} postfix";
        assertTrue("Should contain the value now", expander.expandVariables( expression ).contains( defaultValue ) );
        assertFalse("Should not contain the variable anymore", expander.expandVariables( expression ).contains( variableName ) );
    }

    @Test
    public void testNullDefaultValueVariable() {
        final String variableName = "TEST_VARIABLE_NAME";
        final String expression = "prefix ${" + variableName + ":} postfix";
        thrown.expect( RuntimeException.class );
        thrown.expectMessage( variableName );
        EnvironmentVariableExpander.expand(expression);
    }

    @Test
    public void testRecursiveValueVariable() {
        final String nestedVariableValue = "a value";
        final String nestedVariableName = "NESTED_VARIABLE_NAME";
        final String variableValue = "${" + nestedVariableName + "}";
        final String variableName = "TEST_VARIABLE_NAME";
        final ImmutableMap<String,String> environment = ImmutableMap.of( variableName, variableValue, nestedVariableName, nestedVariableValue);
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander( environment::get );

        final String expression = "prefix ${" + variableName + "} postfix";
        assertTrue("Should contain the value now", expander.expandVariables( expression ).contains( nestedVariableValue ) );
        assertFalse("Should not contain the nested variable name", expander.expandVariables( expression ).contains( nestedVariableName ) );
        assertFalse("Should not contain the variable anymore", expander.expandVariables( expression ).contains( variableName ) );
    }

    @Test
    public void testSpecialCharacters() {
        final String variableValue = "a value with a reference $7";
        final String variableName = "TEST_VARIABLE_NAME";
        final ImmutableMap<String,String> environment = ImmutableMap.of( variableName, variableValue );
        final EnvironmentVariableExpander expander = new EnvironmentVariableExpander( environment::get );

        final String expression = "prefix ${" + variableName + "} postfix";
        assertTrue("Should contain the value now", expander.expandVariables( expression ).contains( variableValue ) );
        assertFalse("Should not contain the variable anymore", expander.expandVariables( expression ).contains( variableName ) );
    }
}
