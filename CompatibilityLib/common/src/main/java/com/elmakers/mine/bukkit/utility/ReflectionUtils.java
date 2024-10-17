package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.boss.BossBar;
import org.bukkit.inventory.ItemStack;

public class ReflectionUtils {
    private static Field itemStackHandleField;
    private static Field bossBarHandleField;
    private static Method nbtListGetMethod;

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

    public static Object getHandle(Logger logger, BossBar bossBar) {
        try {
            if (bossBarHandleField == null) {
                bossBarHandleField = bossBar.getClass().getDeclaredField("handle");
                bossBarHandleField.setAccessible(true);
            }
            return bossBarHandleField.get(bossBar);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting handle of " + bossBar.getClass().getName(), ex);
        }
        return null;
    }

    public static Object getHandle(Logger logger, ItemStack itemStack, Class<?> c) {
        try {
            if (itemStackHandleField == null) {
                itemStackHandleField = c.getDeclaredField("handle");
                itemStackHandleField.setAccessible(true);
            }
            return itemStackHandleField.get(itemStack);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting handle of " + itemStack.getClass().getName(), ex);
        }
        return null;
    }

    public static Object getListItem(Logger logger, Object listTag, int index) {
        try {
            if (nbtListGetMethod == null) {
                nbtListGetMethod = listTag.getClass().getMethod("get", Integer.TYPE);
            }
            return nbtListGetMethod.invoke(listTag, index);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting list item from " + listTag.getClass().getName(), ex);
        }
        return null;
    }

    public static boolean setPrivateNeedsFixing(Logger logger, Object o, Class<?> c, String field, String fixedField, Object value) {
        return setPrivate(logger, o, c, fixedField, value);
    }

    public static Object getPrivateNeedsFixing(Logger logger, Object o, Class<?> c, String field, String fixedField) {
        return getPrivate(logger, o, c, fixedField);
    }

    public static boolean setPrivate(Logger logger, Object o, Class<?> c, String field, Object value) {
        try {
            Field access = c.getDeclaredField(field);
            access.setAccessible(true);
            access.set(o, value);
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error setting private member of " + o.getClass().getName() + "." + field, ex);
        }
        return false;
    }

    public static boolean hasMethod(Class<?> c, String field, Class<?>[] parameters) {
        try {
            Method method = c.getDeclaredMethod(field, parameters);
            // This won't ever be null, but we can't use the getDeclaredMethod without using the return
            // value or else checkstyle gets cranky :\
            return method != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean callPrivate(Logger logger, Object o, Class<?> c, String field, Class<?>[] parameters, Object[] values) {
        try {
            Method method = c.getDeclaredMethod(field, parameters);
            method.setAccessible(true);
            method.invoke(o, values);
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error calling private method of " + o.getClass().getName() + "." + field, ex);
        }
        return false;
    }
}
