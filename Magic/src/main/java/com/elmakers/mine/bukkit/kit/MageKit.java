package com.elmakers.mine.bukkit.kit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.item.ItemData;

public class MageKit {
    private final String key;
    private long lastGive;
    private final Map<String, GivenItem> givenItems = new HashMap<>();

    public MageKit(String kitKey) {
        this.key = kitKey;
    }

    public void give(ItemData itemData) {
        lastGive = System.currentTimeMillis();
        GivenItem given = givenItems.get(itemData.getKey());
        if (given == null) {
            given = new GivenItem(itemData);
            givenItems.put(itemData.getKey(), given);
        } else {
            given.add(itemData.getAmount());
        }
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
