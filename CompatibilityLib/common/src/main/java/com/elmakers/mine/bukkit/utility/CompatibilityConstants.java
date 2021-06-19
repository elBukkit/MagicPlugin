package com.elmakers.mine.bukkit.utility;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class CompatibilityConstants {
    public static boolean DEBUG = false;

    public static final int NBT_TYPE_COMPOUND = 10;
    public static final int NBT_TYPE_INT_ARRAY = 11;
    public static final int NBT_TYPE_INTEGER = 3;
    public static final int NBT_TYPE_FLOAT = 5;
    public static final int NBT_TYPE_DOUBLE = 6;
    public static final int NBT_TYPE_STRING = 8;
    public static final int FIREWORK_TYPE = 76;
    public static final UUID SKULL_UUID = UUID.fromString("3f599490-ca3e-49b5-8e75-78181ebf4232");

    public static int MAX_LORE_LENGTH = 24;
    public static int MAX_PROPERTY_DISPLAY_LENGTH = 50;

    public static String getVersionPrefix() {
        String versionPrefix = "";
        String className = Bukkit.getServer().getClass().getName();
        String[] packages = StringUtils.split(className, '.');
        if (packages.length == 5) {
            versionPrefix = packages[3] + ".";
        }

        return versionPrefix;
    }
}
