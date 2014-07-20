package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.utility.Messages;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A represents a randomized upgrade path that a wand may use
 * when upgrading.
 *
 * Upgrading is generally done by spending XP on an enchanting table.
 */
public class WandUpgradePath {
    private static Map<String, WandUpgradePath> paths = new HashMap<String, WandUpgradePath>();

    private TreeMap<Integer, WandLevel> levelMap = null;
    private int[] levels = null;
    private final String key;
    private final WandUpgradePath parent;
    private final Set<String> spells = new HashSet<String>();
    private String name;
    private String description;

    private int maxUses = 500;
    private int maxMaxXp = 1500;
    private int maxXpRegeneration = 150;
    private float maxHungerRegeneration = 2;
    private float maxHealthRegeneration = 2;
    private float maxDamageReduction = 0.4f;
    private float maxDamageReductionExplosions = 0.3f;
    private float maxDamageReductionFalling = 0.9f;
    private float maxDamageReductionFire = 0.5f;
    private float maxDamageReductionPhysical = 0.1f;
    private float maxDamageReductionProjectiles = 0.2f;
    private float maxCostReduction = 0.5f;
    private float maxCooldownReduction = 0.5f;
    private float maxHaste = 1;
    private float maxPower = 1;
    private int minLevel = 10;
    private int maxLevel = 50;

    public WandUpgradePath(String key, WandUpgradePath inherit, ConfigurationSection template)
    {
        this.parent = inherit;
        this.key = key;
        this.levels = inherit.levels;
        this.maxMaxXp = inherit.maxMaxXp;
        this.maxXpRegeneration = inherit.maxXpRegeneration;
        this.maxHungerRegeneration = inherit.maxHungerRegeneration;
        this.maxHealthRegeneration = inherit.maxHealthRegeneration;
        this.maxDamageReduction = inherit.maxDamageReduction;
        this.maxDamageReductionExplosions = inherit.maxDamageReductionExplosions;
        this.maxDamageReductionFalling = inherit.maxDamageReductionFalling;
        this.maxDamageReductionFire = inherit.maxDamageReductionFire;
        this.maxDamageReductionPhysical = inherit.maxDamageReductionPhysical;
        this.maxDamageReductionProjectiles = inherit.maxDamageReductionProjectiles;
        this.maxCostReduction = inherit.maxCostReduction;
        this.maxHaste = inherit.maxHaste;
        this.maxPower = inherit.maxPower;
        this.minLevel = inherit.minLevel;
        this.maxLevel = inherit.maxLevel;
        this.levelMap = new TreeMap<Integer, WandLevel>(inherit.levelMap);
        load(key, template);
    }

    public WandUpgradePath(String key, ConfigurationSection template) {
        this.key = key;
        this.parent = null;
        load(key, template);
    }

    protected void load(String key, ConfigurationSection template) {
        ConfigurationSection spellSection = template.getConfigurationSection("spells");
        if (spellSection != null) {
            spells.addAll(spellSection.getKeys(false));
        }
        name = template.getString("name", name);
        name = Messages.get("paths." + key + ".name", name);
        description = template.getString("description", description);
        description = Messages.get("paths." + key + ".description", description);

        // Fetch overall limits
        maxUses = template.getInt("max_uses", maxUses);
        maxMaxXp = template.getInt("max_mana", maxMaxXp);
        maxXpRegeneration = template.getInt("max_mana_regeneration", maxXpRegeneration);
        maxHealthRegeneration = (float)template.getDouble("max_health_regeneration", maxHealthRegeneration);
        maxHungerRegeneration = (float)template.getDouble("max_hunger_regeneration", maxHungerRegeneration);

        minLevel = template.getInt("min_enchant_level", minLevel);
        maxLevel = template.getInt("max_enchant_level", maxLevel);

        maxDamageReduction = (float)template.getDouble("max_damage_reduction", maxDamageReduction);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_explosions", maxDamageReductionExplosions);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_falling", maxDamageReductionFalling);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_fire", maxDamageReductionFire);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_physical", maxDamageReductionPhysical);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_projectiles", maxDamageReductionProjectiles);
        maxCostReduction = (float)template.getDouble("max_cost_reduction", maxCostReduction);
        maxCooldownReduction = (float)template.getDouble("max_cooldown_reduction", maxCooldownReduction);
        maxHaste = (float)template.getDouble("max_haste", maxHaste);

        // Parse defined levels
        if (levelMap == null) {
            levelMap = new TreeMap<Integer, WandLevel>();
        }
        if (template.contains("levels")) {
            String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
            levels = new int[levelStrings.length];
            for (int i = 0; i < levels.length; i++) {
                levels[i] = Integer.parseInt(levelStrings[i]);
            }
        }

        if (levels == null) {
           return;
        }

        for (int level = 1; level <= levels[levels.length - 1]; level++) {
            // TODO: Could this be optimized?
            int levelIndex;
            int nextLevelIndex = 0;
            float distance = 1;
            for (levelIndex = 0; levelIndex < levels.length; levelIndex++) {
                if (level == levels[levelIndex] || levelIndex == levels.length - 1) {
                    nextLevelIndex = levelIndex;
                    distance = 0;
                    break;
                }

                if (level > levels[levelIndex]) {
                    nextLevelIndex = levelIndex + 1;
                    int previousLevel = levels[levelIndex];
                    int nextLevel = levels[nextLevelIndex];
                    distance = (float)(level - previousLevel) / (float)(nextLevel - previousLevel);
                }
            }

            WandLevel wandLevel = levelMap.get(level);
            WandLevel newLevel = new WandLevel(this, template, levelIndex, nextLevelIndex, distance);
            if (wandLevel == null) {
                wandLevel = newLevel;
            } else {
                newLevel.add(wandLevel);
                wandLevel = newLevel;
            }
            levelMap.put(level, wandLevel);
        }
    }

    public String getKey() {
        return key;
    }

    public WandLevel getLevel(int level) {
        if (levelMap == null) return null;

        if (!levelMap.containsKey(level)) {
            if (level > levelMap.lastKey()) {
                return levelMap.lastEntry().getValue();
            }

            return levelMap.firstEntry().getValue();
        }

        return levelMap.get(level);
    }

    protected static WandUpgradePath getPath(String key, ConfigurationSection configuration)
    {
        WandUpgradePath path = paths.get(key);
        if (path == null) {
            ConfigurationSection parameters = configuration.getConfigurationSection(key);
            if (!parameters.getBoolean("enabled", true)) {
                return null;
            }
            String inheritKey = parameters.getString("inherit");
            if (inheritKey != null && !inheritKey.isEmpty()) {
                WandUpgradePath inherit = getPath(inheritKey, configuration);
                if (inherit == null) {
                    Bukkit.getLogger().warning("Failed to load inherited enchanting path '" + inheritKey + "' for path: " + key);
                    return null;
                }
                path = new WandUpgradePath(key, inherit, parameters);
            } else {
                path = new WandUpgradePath(key, parameters);
            }

            paths.put(key, path);
        }

        return path;
    }

    public static void loadPaths(ConfigurationSection configuration) {
        paths.clear();
        Set<String> pathKeys = configuration.getKeys(false);
        for (String key : pathKeys)
        {
            getPath(key, configuration);
        }
    }

    public static Set<String> getPathKeys() {
        return paths.keySet();
    }

    public static WandUpgradePath getPath(String key) {
        return paths.get(key);
    }

    public int getMaxLevel() {
        if (levels == null) return 0;

        return Math.min(levels[levels.length - 1], maxLevel);
    }

    public Set<Integer> getLevels() {
        if (levels == null) return null;
        Set<Integer> filteredLevels = new HashSet<Integer>();
        for (Integer level : levels) {
            if (level >= minLevel && level <= maxLevel) {
                filteredLevels.add(level);
            }
        }
        return filteredLevels;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getMaxMaxXp() {
        return maxMaxXp;
    }

    public int getMaxXpRegeneration() {
        return maxXpRegeneration;
    }

    public float getMaxHungerRegeneration() {
        return maxHungerRegeneration;
    }

    public float getMaxHealthRegeneration() {
        return maxHealthRegeneration;
    }

    public float getMaxDamageReduction() {
        return maxDamageReduction;
    }

    public float getMaxDamageReductionExplosions() {
        return maxDamageReductionExplosions;
    }

    public float getMaxDamageReductionFalling() {
        return maxDamageReductionFalling;
    }

    public float getMaxDamageReductionFire() {
        return maxDamageReductionFire;
    }

    public float getMaxDamageReductionPhysical() {
        return maxDamageReductionPhysical;
    }

    public float getMaxDamageReductionProjectiles() {
        return maxDamageReductionProjectiles;
    }

    public float getMaxCostReduction() {
        return maxCostReduction;
    }

    public float getMaxCooldownReduction() {
        return maxCooldownReduction;
    }

    public float getMaxHaste() {
        return maxHaste;
    }

    public float getMaxPower() {
        return maxPower;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public boolean hasSpell(String spellKey) {
        return spells.contains(spellKey);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
