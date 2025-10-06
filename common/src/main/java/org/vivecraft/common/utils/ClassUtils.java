package org.vivecraft.common.utils;

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
}
