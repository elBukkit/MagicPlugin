package com.elmakers.mine.bukkit.utility;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class CompatibilityConstants {
    public static boolean DEBUG = false;
    public static int MAX_LORE_LENGTH = 24;
    public static int MAX_PROPERTY_DISPLAY_LENGTH = 50;
    public static UUID SKULL_UUID = UUID.fromString("3f599490-ca3e-49b5-8e75-78181ebf4232");

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
