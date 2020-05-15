package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;

public class Trigger {
    @Nonnull
    private final String trigger;
    private final int interval;
    private final double maxHealth;
    private final double minHealth;
    private final double maxHealthPercentage;
    private final double minHealthPercentage;
    private final double maxDamage;
    private final double minDamage;
    private final double maxBowPull;
    private final double minBowPull;
    private final boolean isCancelLaunch;

    private long lastTrigger;

    public Trigger(ConfigurationSection configuration) {
        this(configuration, "");
    }

    public Trigger(ConfigurationSection configuration, String defaultType) {
        trigger = configuration.getString("trigger", configuration.getString("type", defaultType)).toLowerCase();
        interval = configuration.getInt("interval");
        maxHealth = configuration.getDouble("max_health");
        minHealth = configuration.getDouble("min_health");
        maxHealthPercentage = configuration.getDouble("max_health_percentage");
        minHealthPercentage = configuration.getDouble("min_health_percentage");
        maxDamage = configuration.getDouble("max_damage");
        minDamage = configuration.getDouble("min_damage");
        isCancelLaunch = configuration.getBoolean("cancel_launch", true);
        maxBowPull = configuration.getDouble("max_bow_pull");
        minBowPull = configuration.getDouble("min_bow_pull");
    }

    public int getInterval() {
        return interval;
    }

    @Nonnull
    public String getTrigger() {
        return trigger;
    }

    public boolean isValid(Mage mage) {
        double damage = mage.getLastDamage();

        if (minDamage > 0 && damage < minDamage) return false;
        if (maxDamage > 0 && damage > maxDamage) return false;

        if (minHealth > 0 && (mage.getHealth() < minHealth)) return false;
        if (maxHealth > 0 && (mage.getHealth() > maxHealth)) return false;
        if (minHealthPercentage > 0 && (mage.getHealth() * 100 / mage.getMaxHealth() < minHealthPercentage)) return false;
        if (maxHealthPercentage > 0 && (mage.getHealth() * 100 / mage.getMaxHealth() > maxHealthPercentage)) return false;

        if (minBowPull > 0 && (mage.getLastBowPull() < minBowPull)) return false;
        if (maxBowPull > 0 && (mage.getLastBowPull() > maxBowPull)) return false;

        if (interval > 0 && System.currentTimeMillis() < lastTrigger + interval) return false;

        return true;
    }

    public void triggered() {
        lastTrigger = System.currentTimeMillis();
    }

    public boolean isCancelLaunch() {
        return isCancelLaunch;
    }
}
