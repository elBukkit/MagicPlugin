package com.elmakers.mine.bukkit.plugins.magic;

import java.util.LinkedList;
import java.util.List;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class RandomUtils extends com.elmakers.mine.bukkit.utility.RandomUtils {
	public static <T extends Object> void populateProbabilityMap(Class<T> valueClass, LinkedList<WeightedPair<T>> probabilityMap, ConfigurationNode nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		Float currentThreshold = 0.0f;
		
		if (nodeMap != null) {
			List<String> keys = nodeMap.getKeys();
			for (String key : keys) {
				currentThreshold += lerp(nodeMap.getString(key).split(","), levelIndex, nextLevelIndex, distance);
				probabilityMap.add(new WeightedPair<T>(currentThreshold, key, valueClass));
			}
		}
	}
	
	public static void populateFloatProbabilityMap(LinkedList<WeightedPair<Float>> probabilityMap, ConfigurationNode nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		populateProbabilityMap(Float.class, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
	}
	
	public static void populateStringProbabilityMap(LinkedList<WeightedPair<String>> probabilityMap, ConfigurationNode nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		populateProbabilityMap(String.class, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
	}
	
	public static void populateIntegerProbabilityMap(LinkedList<WeightedPair<Integer>> probabilityMap, ConfigurationNode nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		populateProbabilityMap(Integer.class, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
	}
}
