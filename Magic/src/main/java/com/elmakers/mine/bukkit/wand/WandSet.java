package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.magic.MagicController;

public class WandSet {
    private final String key;
    private WandProperties bonus;
    private int requiredCount;

    public WandSet(MagicController controller, String key, ConfigurationSection config) {
        this.key = key;
        requiredCount = config.getInt("required");
        ConfigurationSection bonusConfig = config.getConfigurationSection("bonuses");
        if (bonusConfig != null) {
            bonus = new WandProperties(controller, bonusConfig);
        }
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public WandProperties getBonus() {
        return bonus;
    }

    public String getName(Messages messages) {
        return messages.get("wand_sets." + key + ".name", key);
    }
}
