package com.elmakers.mine.bukkit.magic;

import org.bukkit.configuration.ConfigurationSection;

public class DamageType {
    private double maxReduction;
    private double maxAttackMultiplier;
    private double maxDefendMultiplier;

    public DamageType(ConfigurationSection configuration) {
        maxReduction = configuration.getDouble("max_reduction", 0);
        maxAttackMultiplier = configuration.getDouble("max_strength", 0);
        maxDefendMultiplier = configuration.getDouble("max_weakness", 0);
    }

    public double getMaxReduction() {
        return maxReduction;
    }

    public double getMaxAttackMultiplier() {
        return maxAttackMultiplier;
    }

    public double getMaxDefendMultiplier() {
        return maxDefendMultiplier;
    }
}
