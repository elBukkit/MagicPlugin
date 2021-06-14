package com.elmakers.mine.bukkit.utility.platform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is a helper class to encapsulate a reflected constructor for a particular NBT tag class.
 *
 * This is here to bridge the static construction method of 1.15, while falling back to direct constructor access,
 * including private constructors.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NBTConstructor {
    private Constructor constructor;
    private Method staticConstructor;

    public NBTConstructor(Class<?> tagClass, Class<?> typeClass) throws NoSuchMethodException {
        // First try to find a matching static method
        try {
            staticConstructor = tagClass.getMethod("a", typeClass);
            if (!tagClass.isAssignableFrom(staticConstructor.getReturnType())) {
                staticConstructor = null;
            }
        } catch(Exception ignore) {
        }

        if (staticConstructor == null) {
            constructor = tagClass.getDeclaredConstructor(typeClass);
            constructor.setAccessible(true);
        }
    }

    public Object newInstance(Object o) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (staticConstructor != null) {
            return staticConstructor.invoke(null, o);
        }

        return constructor.newInstance(o);
    }
}
