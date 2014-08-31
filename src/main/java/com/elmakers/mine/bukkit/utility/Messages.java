package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class Messages implements com.elmakers.mine.bukkit.api.magic.Messages {
    private static String PARAMETER_PATTERN_STRING = "\\$([^ :]+)";
    private static Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_PATTERN_STRING);
    private static Random random = new Random();

    private Map<String, String> messageMap = new HashMap<String, String>();
    private Map<String, List<String>> randomized = new HashMap<String, List<String>>();
    private ConfigurationSection configuration = null;

    public Messages() {

    }

    public void load(ConfigurationSection messages) {
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

    public List<String> getAll(String path) {
        if (configuration == null) return new ArrayList<String>();
        return configuration.getStringList(path);
    }

    public void reset() {
        messageMap.clear();
    }

    public String get(String key, String defaultValue) {
        return messageMap.containsKey(key) ? ChatColor.translateAlternateColorCodes('&', messageMap.get(key)) : defaultValue;
    }

    public String get(String key) {
        return get(key, key);
    }

    public String getParameterized(String key, String paramName, String paramValue) {
        return get(key, key).replace(paramName, paramValue);
    }

    public String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2) {
        return get(key, key).replace(paramName1, paramValue1).replace(paramName2, paramValue2);
    }

    public String getRandomized(String key) {
        if (!randomized.containsKey(key)) return "";
        List<String> options = randomized.get(key);
        if (options.size() == 0) return "";
        return options.get(random.nextInt(options.size()));
    }

    public String escape(String source) {
        Matcher matcher = PARAMETER_PATTERN.matcher(source);
        String result = source;
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key != null) {
                result = result.replace("$" + key, getRandomized(key));
            }
        }

        return result;
    }
}
