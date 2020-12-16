package com.elmakers.mine.bukkit.api.magic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;

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
    private final String damageType;
    private final Set<String> damageTypes;
    private final Set<EntityType> projectileTypes;

    private long lastTrigger;

    public Trigger(MageController controller, ConfigurationSection configuration) {
        this(controller, configuration, "");
    }

    public Trigger(MageController controller, String triggerKey) {
        this(controller, new MemoryConfiguration(), triggerKey);
    }

    public Trigger(MageController controller, ConfigurationSection configuration, String defaultType) {
        trigger = configuration.getString("trigger", configuration.getString("type", defaultType)).toLowerCase();
        interval = configuration.getInt("interval");
        maxHealth = configuration.getDouble("max_health");
        minHealth = configuration.getDouble("min_health");
        maxHealthPercentage = configuration.getDouble("max_health_percentage");
        minHealthPercentage = configuration.getDouble("min_health_percentage");
        maxDamage = configuration.getDouble("max_damage");
        minDamage = configuration.getDouble("min_damage");
        isCancelLaunch = configuration.getBoolean("cancel_launch", true);
        maxBowPull = configuration.getDouble("max_bowpull");
        minBowPull = configuration.getDouble("min_bowpull");
        damageType = configuration.getString("damage_type");
        List<String> damageTypeList = configuration.getStringList("damage_types");
        damageTypeList.replaceAll(String::toLowerCase);
        damageTypes = damageTypeList.isEmpty() ? null : new HashSet<>(damageTypeList);
        List<String> projectileTypeList = configuration.getStringList("projectile_types");
        String projectileType = configuration.getString("projectile_type");
        if (projectileType != null && !projectileType.isEmpty()) {
            projectileTypeList.add(projectileType);
        }
        if (projectileTypeList.isEmpty()) {
            projectileTypes = null;
        } else {
            projectileTypes = new HashSet<>();
            for (String t : projectileTypeList) {
                try {
                    EntityType entityType = EntityType.valueOf(t.toUpperCase());
                    projectileTypes.add(entityType);
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid projectile type: " + t);
                }
            }
        }
    }

    public int getInterval() {
        return interval;
    }

    @Nonnull
    public String getTrigger() {
        return trigger;
    }

    public boolean isValid(Mage mage) {
        String lastDamageType = null;
        double damage = 0;
        if (trigger.equalsIgnoreCase("damage_dealt")) {
            lastDamageType = mage.getLastDamageDealtType();
            damage = mage.getLastDamage();
        } else {
            lastDamageType = mage.getLastDamageType();
            damage = mage.getLastDamageDealt();
        }

        if (minDamage > 0 && damage < minDamage) return false;
        if (maxDamage > 0 && damage > maxDamage) return false;

        if (minHealth > 0 && (mage.getHealth() < minHealth)) return false;
        if (maxHealth > 0 && (mage.getHealth() > maxHealth)) return false;
        if (minHealthPercentage > 0 && (mage.getHealth() * 100 / mage.getMaxHealth() < minHealthPercentage)) return false;
        if (maxHealthPercentage > 0 && (mage.getHealth() * 100 / mage.getMaxHealth() > maxHealthPercentage)) return false;

        if (minBowPull > 0 && (mage.getLastBowPull() < minBowPull)) return false;
        if (maxBowPull > 0 && (mage.getLastBowPull() > maxBowPull)) return false;

        if (interval > 0 && System.currentTimeMillis() < lastTrigger + interval) return false;

        if (damageType != null && !damageType.isEmpty()) {
            if (lastDamageType == null || !lastDamageType.equalsIgnoreCase(damageType)) return false;
        }
        EntityType lastProjectileType = mage.getLastProjectileType();
        if (damageTypes != null && (lastDamageType == null || !damageTypes.contains(lastDamageType.toLowerCase()))) return false;
        if (projectileTypes != null && (lastProjectileType == null || !projectileTypes.contains(lastProjectileType))) return false;
        return true;
    }

    public void triggered() {
        lastTrigger = System.currentTimeMillis();
    }

    public boolean isCancelLaunch() {
        return isCancelLaunch;
    }
}
