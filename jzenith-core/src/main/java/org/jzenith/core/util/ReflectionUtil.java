package org.jzenith.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtil {

    public static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
