package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {
    public static Object getPrivate(Logger logger, Object o, Class<?> c, String field) {
        try {
            Field access = c.getDeclaredField(field);
            access.setAccessible(true);
            return access.get(o);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting private member of " + o.getClass().getName() + "." + field, ex);
        }
        return null;
    }

    public static boolean setPrivate(Logger logger, Object o, Class<?> c, String field, Object value) {
        try {
            Field access = c.getDeclaredField(field);
            access.setAccessible(true);
            access.set(o, value);
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting private member of " + o.getClass().getName() + "." + field, ex);
        }
        return false;
    }
}
