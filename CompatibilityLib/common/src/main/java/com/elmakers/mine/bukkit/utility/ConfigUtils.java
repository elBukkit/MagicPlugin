package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.configuration.TranslatingConfigurationSection;

/**
 * This was originally part of EffectLib, but I wanted to make an independent copy to use for Magic.
 */
public class ConfigUtils {

    public static Collection<ConfigurationSection> getNodeList(ConfigurationSection node, String path) {
        Collection<ConfigurationSection> results = new ArrayList<>();
        List<Map<?, ?>> mapList = node.getMapList(path);
        for (Map<?, ?> map : mapList) {
            results.add(toConfigurationSection(node, map));
        }

        return results;
    }

    public static ConfigurationSection newSection(ConfigurationSection parent, String path) {
        if (parent instanceof TranslatingConfigurationSection) {
            return ((TranslatingConfigurationSection)parent).newSection(path);
        }
        return new TranslatingConfigurationSection(parent, path);
    }

    public static ConfigurationSection toConfigurationSection(ConfigurationSection parent, Map<?, ?> nodeMap) {
        return toConfigurationSection(parent, "", nodeMap);
    }

    public static ConfigurationSection toConfigurationSection(ConfigurationSection parent, String path, Map<?, ?> nodeMap) {
        ConfigurationSection newSection = newSection(parent, path);
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

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<Object> getList(ConfigurationSection section, String path) {
        List<Object> list = (List<Object>)section.getList(path);
        if (list != null) {
            return list;
        }
        Object o = section.get(path);
        return getList(o);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<Object> getList(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof List) {
            return (List<Object>) o;
        } else if (o instanceof String) {
            return new ArrayList<>(Arrays.asList(StringUtils.split((String) o, ',')));
        } else {
            List<Object> single = new ArrayList<>();
            single.add(o);
            return single;
        }
    }

    public static List<String> getStringList(ConfigurationSection section, String path, List<String> def) {
        List<String> list = getStringList(section, path);
        return list == null ? (def == null ? new ArrayList<>() : def) : list;
    }

    @Nullable
    public static List<String> getStringList(ConfigurationSection section, String path) {
        List<Object> raw = getList(section, path);
        return getStringList(raw);
    }

    @Nullable
    public static List<String> getStringList(ConfigurationSection section, String path, String delimiter) {
        if (section.isList(path)) {
            List<Object> raw = getList(section, path);
            return getStringList(raw);
        }
        String value = section.getString(path);
        if (value == null) {
            return null;
        }
        String[] pieces = StringUtils.split(value, delimiter);
        return Arrays.asList(pieces);
    }

    @Nullable
    public static List<String> getStringList(Object o) {
        List<Object> raw = getList(o);
        return getStringList(raw);
    }

    @Nullable
    public static List<String> getStringList(List<Object> rawList) {
        if (rawList == null) {
            return null;
        }

        List<String> list = new ArrayList<>();

        for (Object o : rawList) {
            // This prevents weird behaviors when we're expecting a list of strings but given
            // a list of ConfigurationSections, Lists or other complex types
            if (o == null || o instanceof ConfigurationSection || o instanceof List || o instanceof Map) {
                continue;
            }
            list.add(o.toString());
        }

        return list;
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log) {
        return getPotionEffectObjects(baseConfig, key, log, Integer.MAX_VALUE, 0, true, true);
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log, int defaultDuration) {
        return getPotionEffectObjects(baseConfig, key, log, defaultDuration, 0, true, true);
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log, int defaultDuration, int defaultAmplifier, boolean defaultAmbient, boolean defaultParticles) {
        List<PotionEffect> potionEffects = null;
        List<Object> genericList = getList(baseConfig, key);
        if (genericList != null && !genericList.isEmpty()) {
            potionEffects = new ArrayList<>();
            for (Object genericEntry : genericList) {
                if (genericEntry instanceof String) {
                    String typeString = (String)genericEntry;
                    PotionEffectType effectType = PotionEffectType.getByName(typeString.toUpperCase());
                    if (effectType == null) {
                        log.log(Level.WARNING, "Invalid potion effect type: " + typeString);
                        continue;
                    }
                    int ticks = defaultDuration / 50;
                    potionEffects.add(new PotionEffect(effectType, effectType.isInstant() ? 1 : ticks, defaultAmplifier, defaultAmbient, defaultParticles));
                } else {
                    ConfigurationSection potionEffectSection = genericEntry instanceof ConfigurationSection ? (ConfigurationSection)genericEntry : null;
                    if (potionEffectSection == null && genericEntry instanceof Map) {
                        potionEffectSection = toConfigurationSection(baseConfig, (Map<?, ?>)genericEntry);
                    }
                    if (potionEffectSection != null) {
                        if (potionEffectSection.contains("type")) {
                            PotionEffectType effectType = PotionEffectType.getByName(potionEffectSection.getString("type").toUpperCase());
                            if (effectType == null) {
                                log.log(Level.WARNING, "Invalid potion effect type: " + potionEffectSection.getString("type", "(null)"));
                                continue;
                            }
                            int ticks = Integer.MAX_VALUE;
                            String duration = potionEffectSection.getString("duration");
                            if (duration == null || (!duration.equals("forever") && !duration.equals("infinite") && !duration.equals("infinity"))) {
                                ticks = (int) (potionEffectSection.getLong("duration", defaultDuration) / 50);
                                ticks = potionEffectSection.getInt("ticks", ticks);
                            }
                            int amplifier = potionEffectSection.getInt("amplifier", defaultAmplifier);
                            boolean ambient = potionEffectSection.getBoolean("ambient", defaultAmbient);
                            boolean particles = potionEffectSection.getBoolean("particles", defaultParticles);

                            potionEffects.add(new PotionEffect(effectType, effectType.isInstant() ? 1 : ticks, amplifier, ambient, particles));
                        } else {
                            Collection<String> types = potionEffectSection.getKeys(false);
                            for (String type : types) {
                                PotionEffectType effectType = PotionEffectType.getByName(type.toUpperCase());
                                if (effectType == null) {
                                    log.log(Level.WARNING, "Invalid potion effect type: " + type);
                                    continue;
                                }
                                int amplifier = potionEffectSection.getInt(type, defaultAmplifier);
                                int ticks = defaultDuration / 50;
                                potionEffects.add(new PotionEffect(effectType, effectType.isInstant() ? 1 : ticks, amplifier, defaultAmbient, defaultParticles));
                            }
                        }
                    }
                }
            }
        }
        return potionEffects;
    }
}
