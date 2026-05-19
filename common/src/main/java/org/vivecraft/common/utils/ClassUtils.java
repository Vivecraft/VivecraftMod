package org.vivecraft.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassUtils {

    /**
     * does a class Lookup with an alternative
     *
     * @param class1 first option
     * @param class2 alternative option
     * @return found class
     * @throws ClassNotFoundException if neither class exists
     */
    public static Class<?> getClassWithAlternative(String class1, String class2) throws ClassNotFoundException {
        try {
            return Class.forName(class1);
        } catch (ClassNotFoundException e) {
            return Class.forName(class2);
        }
    }

    /**
     * does a field Lookup with an alternative
     *
     * @param clazz  Class to get the field from
     * @param field1 first option
     * @param field2 alternative option
     * @return found field
     * @throws NoSuchFieldException if neither field exists
     */
    public static Field getFieldWithAlternative(
        Class<?> clazz, String field1, String field2) throws NoSuchFieldException
    {
        try {
            return clazz.getDeclaredField(field1);
        } catch (NoSuchFieldException e) {
            return clazz.getDeclaredField(field2);
        }
    }

    /**
     * does a method Lookup with an alternative
     *
     * @param clazz   Class to get the method from
     * @param method1 first option
     * @param method2 alternative option
     * @return found method
     * @throws NoSuchMethodException if neither method exists
     */
    public static Method getMethodWithAlternative(
        Class<?> clazz, String method1, String method2) throws NoSuchMethodException
    {
        try {
            return clazz.getDeclaredMethod(method1);
        } catch (NoSuchMethodException e) {
            return clazz.getDeclaredMethod(method2);
        }
    }
}
