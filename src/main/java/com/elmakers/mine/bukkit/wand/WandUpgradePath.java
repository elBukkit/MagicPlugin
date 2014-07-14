package com.elmakers.mine.bukkit.wand;

import org.apache.commons.lang.StringUtils;
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

    public WandUpgradePath(String key, ConfigurationSection template) {
        this.key = key;

        // Parse defined levels
        levelMap = new TreeMap<Integer, WandLevel>();
        String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
        levels = new int[levelStrings.length];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = Integer.parseInt(levelStrings[i]);
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

            levelMap.put(level, new WandLevel(template, levelIndex, nextLevelIndex, distance));
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

    public static void loadPaths(ConfigurationSection configuration) {
        paths.clear();
        Set<String> pathKeys = configuration.getKeys(false);
        for (String key : pathKeys)
        {
            ConfigurationSection parameters = configuration.getConfigurationSection(key);
            if (!parameters.getBoolean("enabled", true)) continue;
            WandUpgradePath path = new WandUpgradePath(key, parameters);
            paths.put(key, path);
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

        return Math.min(levels[levels.length - 1], WandLevel.maxLevel);
    }

    public Set<Integer> getLevels() {
        if (levels == null) return null;
        Set<Integer> filteredLevels = new HashSet<Integer>();
        for (Integer level : levels) {
            if (level >= WandLevel.minLevel && level <= WandLevel.maxLevel) {
                filteredLevels.add(level);
            }
        }
        return filteredLevels;
    }

}
