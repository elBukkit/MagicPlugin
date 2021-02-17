package com.elmakers.mine.bukkit.magic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class MageKit {
    private final String key;
    private long lastGive;
    private final Map<String, Long> givenItems = new HashMap<>();

    public MageKit(String kitKey) {
        this.key = kitKey;
    }

    public void give(String itemKey) {
        lastGive = System.currentTimeMillis();
        givenItems.put(itemKey, lastGive);
    }

    public long getLastGiveTime() {
        return lastGive;
    }

    public static MageKit load(String key, ConfigurationSection kitConfig) {
        MageKit kit = new MageKit(key);
        kit.lastGive = kitConfig.getLong("last_give");
        ConfigurationSection items = kitConfig.getConfigurationSection("given_items");
        if (items != null) {
            for (String itemKey : items.getKeys(false)) {
                kit.givenItems.put(itemKey, items.getLong(itemKey));
            }
        }
        return kit;
    }

    public void saveTo(ConfigurationSection kitConfig) {
        ConfigurationSection kitSection = kitConfig.createSection(key);
        save(kitSection);
    }

    public void save(ConfigurationSection kitConfig) {
        kitConfig.set("last_give", lastGive);
        kitConfig.set("given_items", givenItems);
    }
}
