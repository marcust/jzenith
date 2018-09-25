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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jzenith.core.util.TestUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.spy;

@RunWith(Parameterized.class)
public class TestPublicApi {

    @Parameterized.Parameters(name = "{2}.{3}({4})")
    public static Collection<Object[]> tests() {
        final List<Class<?>> classes = TestUtil.listAllApiClasses();
        final List<Method> testableMethods = classes.stream()
                .map(TestUtil::listTestableMethods)
                .flatMap(List::stream)
                .collect(ImmutableList.toImmutableList());

        final ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
        for (Method m : testableMethods) {
            TestUtil.makeParameterPermutations(m)
            .stream()
            .forEach(parameters -> builder.add(new Object[] {m, parameters, m.getDeclaringClass().getSimpleName(), m.getName(), asList(m.getParameterTypes())}));
        }

        return builder.build();
    }

    private final Method method;
    private final Object[] parameters;


    public TestPublicApi(Method method, Object[] parameters, String className, String methodName, List<Class<?>> parameterTypes) {
        this.method = method;
        this.parameters = parameters;
    }

    @Test
    public void test() throws IllegalAccessException {
        try {
            final Class<?> declaringClass = method.getDeclaringClass();
            if (Modifier.isStatic(method.getModifiers())) {
                method.invoke(declaringClass, parameters);
            } else {
                method.invoke(spy(declaringClass), parameters);
            }
            fail("Method " + method.getName() + "(" + asList(method.getParameterTypes()) + ") should have thrown a Lombok NPE");
        } catch (InvocationTargetException e) {
            assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
            assertThat(e.getCause().getMessage())
                    .as("Method %s %s throws a NPE without message", method.getName(), asList(method.getParameterTypes()).toString())
                    .isNotNull();
            assertThat(e.getCause().getMessage()).contains("marked @NonNull");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error while invoking " + method.getName() + " with " + asList(parameters));
        }

    }




}
