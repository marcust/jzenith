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

import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@UtilityClass
public class TestUtil {

    public static void testPublicMethodsHaveNonNullParameters(final Object object) throws IllegalAccessException {
        final Method[] declaredMethods = object.getClass().getDeclaredMethods();
        for (final Method method : declaredMethods) {
            if ((method.getModifiers() & Modifier.PUBLIC) != 0) {
                if (method.getName().contains("CGLIB") || "equals".equals(method.getName())) {
                    continue;
                }

                final List<Object[]> parameterPermutations = makeParameterPermutations(method);

                for (final Object[] parameters : parameterPermutations) {

                    try {
                        method.invoke(object, parameters);
                        fail("Method " + method.getName() + "(" + Arrays.asList(method.getParameterTypes()) + ") should have thrown a Lombok NPE");
                    } catch (InvocationTargetException e) {
                        assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
                        assertThat(e.getCause().getMessage())
                                .as("Method %s %s throws a NPE without message", method.getName(), Arrays.asList(method.getParameterTypes()).toString())
                                .isNotNull();
                        assertThat(e.getCause().getMessage()).contains("marked @NonNull");
                    }
                }
            }
        }
    }

    private static List<Object[]> makeParameterPermutations(Method method) {
        final int parameterCount = method.getParameterCount();

        if (parameterCount == 0) {
            return ImmutableList.of();
        }

        final ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
        for (int i = 0; i < parameterCount; i++) {
            for (int j = 0; j < parameterCount; j++) {
                final Object[] parameters = new Object[parameterCount];
                for (int k = 0; k < i; k++) {
                    parameters[k] = makeNonNullParameter(method, k);
                }
                for (int l = i; l < parameterCount; l++) {
                    parameters[l] = makeNullParameter(method, l);
                }

                if (hasNullParameter(parameters)) {
                    builder.add(parameters);
                }
            }
        }

        return builder.build();
    }

    private static boolean hasNullParameter(Object[] parameters) {
        return Stream.of(parameters).anyMatch(Objects::isNull);
    }

    private static Object makeNullParameter(Method method, int position) {
        final Class<?> type = getParameterType(method, position);

        if (type.isPrimitive()) {
            return Defaults.defaultValue(type);
        }

        return null;
    }

    private static Object makeNonNullParameter(Method method, int position) {
        final Class<?> type = getParameterType(method, position);

        if (type.isPrimitive()) {
            return Defaults.defaultValue(type);
        }
        if (type == String.class) {
            return "foo";
        }

        return mock(type);

    }

    private static Class<?> getParameterType(Method method, int position) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes[position];
    }

}
