package com.elmakers.mine.bukkit.plugins.magic;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.utilities.RandomUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WandLevel {
	private static TreeMap<Integer, WandLevel> levelMap = null;
	private static int[] levels = null;
	
	private final TreeMap<Float, Integer> spellCountProbability = new TreeMap<Float, Integer>();
	private final TreeMap<Float, String> spellProbability = new TreeMap<Float, String>();
	private final TreeMap<Float, Integer> useProbability = new TreeMap<Float, Integer>();
	private final TreeMap<Float, Integer> addUseProbability = new TreeMap<Float, Integer>();
	
	public static WandLevel getLevel(int level) {
		if (levelMap == null) return null;
		
		if (!levelMap.containsKey(level)) {
			if (level > levelMap.lastKey()) {
				return levelMap.lastEntry().getValue();
			}
			
			return levelMap.firstEntry().getValue();
		}
		
		return levelMap.get(level);
	}
	
	public static void mapLevels(ConfigurationNode template) {
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
		
		// Fetch spell probabilities
		populateStringProbabilityMap(spellProbability, template.getNode("spells"), levelIndex, nextLevelIndex, distance);
		
		// Fetch spell count probabilities
		populateIntegerProbabilityMap(spellCountProbability, template.getNode("spell_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch uses
		populateIntegerProbabilityMap(useProbability, template.getNode("uses"), levelIndex, nextLevelIndex, distance);
		populateIntegerProbabilityMap(addUseProbability, template.getNode("add_uses"), levelIndex, nextLevelIndex, distance);
	}
	
	private static void populateIntegerProbabilityMap(TreeMap<Float, Integer> probabilityMap, ConfigurationNode nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		Float currentThreshold = 0.0f;
		if (nodeMap != null) {
			List<String> keys = nodeMap.getKeys();
			for (String key : keys) {
				currentThreshold += lerp(nodeMap.getString(key).split(","), levelIndex, nextLevelIndex, distance);
				probabilityMap.put(currentThreshold, Integer.parseInt(key));
			}
		}
	}
	
	private static void populateStringProbabilityMap(TreeMap<Float, String> probabilityMap, ConfigurationNode nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		Float currentThreshold = 0.0f;
		if (nodeMap != null) {
			List<String> keys = nodeMap.getKeys();
			for (String key : keys) {
				currentThreshold += lerp(nodeMap.getString(key).split(","), levelIndex, nextLevelIndex, distance);
				probabilityMap.put(currentThreshold, key);
			}
		}
	}
	
	private static float lerp(String[] list, int levelIndex, int nextLevelIndex, float distance) {
		float previousValue = Float.parseFloat(list[levelIndex]);
		float nextValue = Float.parseFloat(list[nextLevelIndex]);
		return previousValue + distance * (nextValue - previousValue);
	}
	
	private void randomizeWand(Wand wand, boolean additive) {
		// Add random spells to the wand
		Spell firstSpell = null;		
		Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
		int retries = 30;
		for (int i = 0; i < spellCount; i++) {
			String spellKey = RandomUtils.weightedRandom(spellProbability);
			
			if (wand.addSpell(spellKey)) {	
				if (firstSpell == null) {
					firstSpell = wand.getMaster().getSpell(spellKey);
				}
			} else {
				// Try again up to a certain number if we picked one the wand already had.
				if (retries-- > 0) i--;
			}
		}
		
		// Add or set uses to the wand
		if (additive) {
			// Only add uses to a wand if it already has some.
			int wandUses = wand.getUses();
			if (wandUses > 0) {
				wand.setUses(wandUses + RandomUtils.weightedRandom(addUseProbability));
				wand.updateName(true);
			}
		} else {
			wand.setUses(RandomUtils.weightedRandom(useProbability));
			
			// If we are creating a new wand, make a templatized name
			// based on the first spell that was added to it.
			String spellName = "Nothing";
			if (firstSpell != null) {
				spellName = firstSpell.getName();
			} 
			String updatedName = wand.getName();
			wand.setName(updatedName.replace("{Spell}", spellName));
		}
	}
	
	public static void randomizeWand(Wand wand, boolean additive, int level) {
		WandLevel wandLevel = getLevel(level);
		wandLevel.randomizeWand(wand, additive);
	}
	
	public static Set<Integer> getLevels() {
		if (levels == null) return null;
		
		return levelMap.keySet();
	}
}
