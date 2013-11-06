package com.elmakers.mine.bukkit.plugins.magic;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.utilities.RandomUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WandLevel {
	private static TreeMap<Integer, WandLevel> levelMap = null;
	private static int[] levels = null;
	
	private final TreeMap<Float, Integer> spellCountProbability = new TreeMap<Float, Integer>();
	private final TreeMap<Float, String> spellProbability = new TreeMap<Float, String>();
	
	public static WandLevel getLevel(int level, ConfigurationNode template) {
		if (levelMap == null) {
			mapLevels(template);
		}
		
		if (!levelMap.containsKey(level)) {
			if (level > levelMap.lastKey()) {
				return levelMap.lastEntry().getValue();
			}
			
			return levelMap.firstEntry().getValue();
		}
		
		return levelMap.get(level);
	}
	
	private static void mapLevels(ConfigurationNode template) {
		// Parse defined levels
		levelMap = new TreeMap<Integer, WandLevel>();
		String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
		levels = new int[levelStrings.length];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = Integer.parseInt(levelStrings[i]);
		}
		
		for (int level = 1; level < levels[levels.length - 1]; level++) {
			levelMap.put(level, new WandLevel(level, template));
		}
	}
	
	private WandLevel(int level, ConfigurationNode template) {
		int levelIndex = 0;
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
		
		Float currentThreshold = 0.0f;
		ConfigurationNode spellCountNode = template.getNode("spell_count_probability");
		if (spellCountNode != null) {
			List<String> keys = spellCountNode.getKeys();
			for (String key : keys) {
				Integer spellCount = Integer.parseInt(key);
				String[] thresholdList = spellCountNode.getString(key).split(",");
				float previousThreshold = Float.parseFloat(thresholdList[levelIndex]);
				float nextThreshold = Float.parseFloat(thresholdList[nextLevelIndex]);
				Float threshold = previousThreshold + distance * (nextThreshold - previousThreshold);
				currentThreshold += threshold;
				spellCountProbability.put(currentThreshold, spellCount);
			}
		}
		
		// Fetch spell probabilities
		currentThreshold = 0.0f;
		ConfigurationNode spellsNode = template.getNode("spells");
		if (spellsNode != null) {
			List<String> keys = spellsNode.getKeys();
			for (String key : keys) {
				String spellName = key;
				String[] thresholdList = spellsNode.getString(key).split(",");
				float previousThreshold = Float.parseFloat(thresholdList[levelIndex]);
				float nextThreshold = Float.parseFloat(thresholdList[nextLevelIndex]);
				Float threshold = previousThreshold + distance * (nextThreshold - previousThreshold);
				currentThreshold += threshold;
				spellProbability.put(currentThreshold, spellName);
			}
		}
	}
	
	private void randomizeWand(Wand wand, boolean additive) {
		Spell firstSpell = null;
		
		Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
		for (int i = 0; i < spellCount; i++) {
			String spellKey = RandomUtils.weightedRandom(spellProbability);
			wand.addSpell(spellKey);
			if (firstSpell == null) {
				firstSpell = wand.getMaster().getSpell(spellKey);
			}
		}
		if (!additive) {
			String spellName = "Nothing";
			if (firstSpell != null) {
				spellName = firstSpell.getName();
			} 
			String updatedName = wand.getName();
			wand.setName(updatedName.replace("{Spell}", spellName));
		}
	}
	
	public static void randomizeWand(Wand wand, boolean additive, int level, ConfigurationNode template) {
		WandLevel wandLevel = getLevel(level, template);
		wandLevel.randomizeWand(wand, additive);
	}
}
