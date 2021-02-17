package com.elmakers.mine.bukkit.kit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class MageKit {
    private final String key;
    private long lastGive;
    private final Map<String, GivenItem> givenItems = new HashMap<>();

    public MageKit(String kitKey) {
        this.key = kitKey;
    }

    public void gave(String itemKey, int itemAmount) {
        lastGive = System.currentTimeMillis();
        GivenItem given = givenItems.get(itemKey);
        if (given == null) {
            given = new GivenItem(itemKey, itemAmount);
            givenItems.put(itemKey, given);
        } else {
            given.add(itemAmount);
        }
    }

    public long getLastGiveTime() {
        return lastGive;
    }

    public int getGivenAmount(String itemKey) {
        GivenItem item = givenItems.get(itemKey);
        return item == null ? 0 : item.getAmount();
    }

    public static MageKit load(String key, ConfigurationSection kitConfig) {
        MageKit kit = new MageKit(key);
        kit.lastGive = kitConfig.getLong("last_give");
        ConfigurationSection items = kitConfig.getConfigurationSection("given_items");
        if (items != null) {
            for (String itemKey : items.getKeys(false)) {
                kit.givenItems.put(itemKey, new GivenItem(itemKey, items.getConfigurationSection(itemKey)));
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
        ConfigurationSection givenSection = kitConfig.createSection("given_items");
        for (GivenItem item : givenItems.values()) {
            item.saveTo(givenSection);
        }
    }
}
