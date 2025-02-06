package com.elmakers.mine.bukkit.utility;

/*
 * This is here to provide a single entry point into ArrayUtils.
 * The goal being to one day phase this out and replace with something else, maybe custom code.
 */
public class ArrayUtils {
    private ArrayUtils() {
        // This is a static singleton
    }

    public static Integer[] toObject(int[] array) {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static Object[] addAll(Object[] array1, Object[] array2) {
        return org.apache.commons.lang3.ArrayUtils.addAll(array1, array2);
    }
}
