package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * This was originally part of EffectLib, but I wanted to make an independent copy to use for Magic.
 */
public class ConfigUtils {

    public static Collection<ConfigurationSection> getNodeList(ConfigurationSection node, String path) {
        Collection<ConfigurationSection> results = new ArrayList<>();
        List<Map<?, ?>> mapList = node.getMapList(path);
        for (Map<?, ?> map : mapList) {
            results.add(toConfigurationSection(map));
        }

        return results;
    }

    @Deprecated
    public static ConfigurationSection toNodeList(Map<?, ?> nodeMap) {
        return toConfigurationSection(nodeMap);
    }

    public static ConfigurationSection toConfigurationSection(Map<?, ?> nodeMap) {
        ConfigurationSection newSection = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : nodeMap.entrySet()) {
            newSection.set(entry.getKey().toString(), entry.getValue());
        }

        return newSection;
    }

    public static ConfigurationSection convertConfigurationSection(Map<?, ?> nodeMap) {
        ConfigurationSection newSection = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : nodeMap.entrySet()) {
            set(newSection, entry.getKey().toString(), entry.getValue());
        }

        return newSection;
    }

    public static ConfigurationSection toStringConfiguration(Map<String, String> stringMap) {
        if (stringMap == null) return null;

        ConfigurationSection configMap = new MemoryConfiguration();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            configMap.set(entry.getKey(), entry.getValue());
        }

        return configMap;
    }


    public static void set(ConfigurationSection node, String path, Object value) {
        if (value == null) {
            node.set(path, null);
            return;
        }

        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            node.set(path, isTrue);
            return;
        }
        try {
            Integer i = (value instanceof Integer) ? (Integer) value : Integer.parseInt(value.toString());
            node.set(path, i);
        } catch (Exception ex) {
            try {
                double d;
                if (value instanceof Double) d = (Double) value;
                else if (value instanceof Float) d = (double) (Float) value;
                else d = Double.parseDouble(value.toString());
                node.set(path, d);
            } catch (Exception ex2) {
                node.set(path, value);
            }
        }
    }

    public static ConfigurationSection getConfigurationSection(ConfigurationSection base, String key) {
        ConfigurationSection section = base.getConfigurationSection(key);
        if (section != null) return section;

        Object value = base.get(key);
        if (value == null) return null;

        if (value instanceof ConfigurationSection) return (ConfigurationSection)value;

        if (value instanceof Map) {
            ConfigurationSection newChild = base.createSection(key);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newChild.set(entry.getKey(), entry.getValue());
            }
            base.set(key, newChild);
            return newChild;
        }

        return null;
    }

    public static boolean isMaxValue(String stringValue) {
        return stringValue.equalsIgnoreCase("infinite")
                || stringValue.equalsIgnoreCase("forever")
                || stringValue.equalsIgnoreCase("infinity")
                || stringValue.equalsIgnoreCase("max");
    }
}
