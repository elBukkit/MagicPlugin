package com.elmakers.mine.bukkit.wand;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ActiveWandSet {
    private final String key;
    private ConfigurationSection configuration;
    private final Map<Wand, ConfigurationSection> wands = new HashMap<>();

    public ActiveWandSet(String key) {
        this.key = key;
    }

    public void add(Wand wand, ConfigurationSection config) {
        // Pull out the bonus config, that is specific to each wand
        ConfigurationSection bonusConfig = null;
        if (config != null) {
            bonusConfig = config.getConfigurationSection("bonuses");
            if (configuration == null) {
                configuration = ConfigurationUtils.cloneConfiguration(config);
                configuration.set("bonuses", null);
            } else {
                // To avoid having to clone or modify config, pull this out and then put it back
                ConfigurationSection templateBonus = configuration.getConfigurationSection("bonuses");
                configuration.set("bonuses", null);
                ConfigurationUtils.addConfigurations(configuration, config, false);
                configuration.set("bonuses", templateBonus);
            }
        }

        wands.put(wand, bonusConfig);
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
        for (Map.Entry<Wand,ConfigurationSection> entry : wands.entrySet()) {
            entry.getKey().applySetBonus(key, entry.getValue());
        }
    }
}
