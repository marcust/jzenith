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
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@UtilityClass
public class TestUtil {

    public static void testPublicMethodsHaveNonNullParameters(final Object object) {
        testPublicMethodsHaveNonNullParameters(object.getClass(), object);
    }

    public static void testPublicMethodsHaveNonNullParameters(final Class<?> clz) {
        testPublicMethodsHaveNonNullParameters(clz, spy(clz));
    }

    @SneakyThrows
    public static void testPublicMethodsHaveNonNullParameters(final Class<?> clz, final Object object) {
        final Iterable<Method> declaredMethods = listTestableMethods(clz);

        for (final Method method : declaredMethods) {
            final List<Object[]> parameterPermutations = makeParameterPermutations(method);

            for (final Object[] parameters : parameterPermutations) {

                try {
                    if (Modifier.isStatic(method.getModifiers())) {
                        method.invoke(clz, parameters);
                    } else {
                        method.invoke(object, parameters);
                    }
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

    private static boolean isNotIgnored(Method method) {
        return !isIgnored(method);
    }

    private static boolean isIgnored(Method method) {
        return method.getName().contains("CGLIB") || "equals".equals(method.getName());
    }

    public static List<Method> listTestableMethods(Class<?> clz) {
        return StreamEx.of(Iterables.concat(Arrays.asList(clz.getDeclaredMethods()),
                Arrays.asList(clz.getMethods())).iterator())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .distinct()
                .filter(TestUtil::isNotIgnored)
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getDeclaringClass() == clz)
                .collect(ImmutableList.toImmutableList());
    }

    public static List<Object[]> makeParameterPermutations(Method method) {
        final int parameterCount = method.getParameterCount();

        if (parameterCount == 0) {
            return ImmutableList.of();
        }

        final ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
        for (int i = 0; i < parameterCount; i++) {
            final Object[] parameters = new Object[parameterCount];

            for (int k = 0; k < parameterCount; k++) {
                parameters[k] = makeNonNullParameter(method, k);
            }

            parameters[i] = makeNullParameter(method, i);

            if (hasNullParameter(parameters)) {
                builder.add(parameters);
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
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }
        if (type == Class.class) {
            return Class.class;
        }
        if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), 0);
        }

        return mock(type);
    }

    private static Class<?> getParameterType(Method method, int position) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes[position];
    }

}
