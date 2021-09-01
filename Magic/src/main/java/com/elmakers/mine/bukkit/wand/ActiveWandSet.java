package com.elmakers.mine.bukkit.wand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ActiveWandSet {
    private final String key;
    private ConfigurationSection configuration;
    private final List<Wand> wands = new ArrayList<>();

    public ActiveWandSet(String key) {
        this.key = key;
    }

    public void add(Wand wand, ConfigurationSection config) {
        wands.add(wand);
        if (config != null) {
            if (configuration == null) {
                configuration = ConfigurationUtils.newConfigurationSection();
            }
            ConfigurationUtils.addConfigurations(configuration, config, false);
        }
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public boolean isActive(WandSet setTemplate) {
        int requiredCount = setTemplate == null ? 0 : setTemplate.getRequiredCount();
        if (configuration != null) {
            requiredCount = configuration.getInt("required", requiredCount);
        }
        return wands.size() >= requiredCount;
    }

    public void applyBonuses() {
        for (Wand wand : wands) {
            wand.applySetBonus(key);
        }
    }
}
