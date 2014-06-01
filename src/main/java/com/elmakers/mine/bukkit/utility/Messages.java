package com.elmakers.mine.bukkit.utility;

import java.util.*;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;


public class Messages {
    public static String PARAMETER_PATTERN_STRING = "\\$([^ :]+)";
    public static Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_PATTERN_STRING);

    private static Map<String, String> messageMap = new HashMap<String, String>();
    private static Map<String, List<String>> randomized = new HashMap<String, List<String>>();
    private static ConfigurationSection configuration = null;
    private static Random random = new Random();

    public static void load(ConfigurationSection messages) {
        configuration = messages;
        Collection<String> keys = messages.getKeys(true);
        for (String key : keys) {
            if (key.equals("randomized")) {
                ConfigurationSection randomSection = messages.getConfigurationSection(key);
                Set<String> randomKeys = randomSection.getKeys(false);
                for (String randomKey : randomKeys) {
                    randomized.put(randomKey, randomSection.getStringList(randomKey));
                }
            } else if (messages.isString(key)) {
                messageMap.put(key, messages.getString(key));
            }
        }
    }

    public static List<String> getAll(String path) {
        if (configuration == null) return new ArrayList<String>();
        return configuration.getStringList(path);
    }

    public static void reset() {
        messageMap.clear();
    }

    public static String get(String key, String defaultValue) {
        return messageMap.containsKey(key) ? ChatColor.translateAlternateColorCodes('&', messageMap.get(key)) : defaultValue;
    }

    public static String get(String key) {
        return get(key, key);
    }

    public static String getParameterized(String key, String paramName, String paramValue) {
        return get(key, key).replace(paramName, paramValue);
    }

    public static String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2) {
        return get(key, key).replace(paramName1, paramValue1).replace(paramName2, paramValue2);
    }

    public static String getRandomized(String key) {
        if (!randomized.containsKey(key)) return "";
        List<String> options = randomized.get(key);
        if (options.size() == 0) return "";
        return options.get(random.nextInt(options.size()));
    }
}
