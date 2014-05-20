package com.elmakers.mine.bukkit.wand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.TreeMap;

/**
 * A represents a randomized upgrade path that a wand may use
 * when upgrading.
 *
 * Upgrading is generally done by spending XP on an enchanting table.
 */
public class WandUpgradePath {
    private TreeMap<Integer, WandLevel> levelMap = null;
    private int[] levels = null;

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

    public void mapLevels(ConfigurationSection template) {
        // Parse defined levels
        levelMap = new TreeMap<Integer, WandLevel>();
        String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
        levels = new int[levelStrings.length];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = Integer.parseInt(levelStrings[i]);
        }

        for (int level = 1; level <= levels[levels.length - 1]; level++) {
            // levelMap.put(level, new WandLevel(level, template));
        }
    }
}
