package com.elmakers.mine.bukkit.utility;

import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Contains some general Randomization utitilies, including
 * computing WeightedPair - based probability decisions.
 */
public class RandomUtils {
	private final static Random random = new Random();
	
	public static float lerp(String[] list, int levelIndex, int nextLevelIndex, float distance) {
		float previousValue = Float.parseFloat(list[levelIndex]);
		float nextValue = Float.parseFloat(list[nextLevelIndex]);
		return previousValue + distance * (nextValue - previousValue);
	}
	
	public static <T extends Object> T weightedRandom(LinkedList<WeightedPair<T>> weightList) {
		if (weightList.size() == 0) return null;
		
		Float maxWeight = weightList.getLast().getThreshold();
		Float selectedWeight = random.nextFloat() * maxWeight;
		for (WeightedPair<T> weight : weightList) {
			if (selectedWeight < weight.getThreshold()) {
				return weight.getValue();
			}
		}
		
		return weightList.getFirst().getValue();
	}
	
    public static void populateIntegerProbabilityMap(LinkedList<WeightedPair<Integer>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		RandomUtils.populateProbabilityMap(Integer.class, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
	}
    
    public static void populateIntegerProbabilityMap(LinkedList<WeightedPair<Integer>> probabilityMap, ConfigurationSection nodeMap) {
		RandomUtils.populateProbabilityMap(Integer.class, probabilityMap, nodeMap, 0, 0, 0);
	}

	public static void populateStringProbabilityMap(LinkedList<WeightedPair<String>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		RandomUtils.populateProbabilityMap(String.class, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
	}

	public static void populateStringProbabilityMap(LinkedList<WeightedPair<String>> probabilityMap, ConfigurationSection nodeMap) {
		RandomUtils.populateProbabilityMap(String.class, probabilityMap, nodeMap, 0, 0, 0);
	}

	public static void populateFloatProbabilityMap(LinkedList<WeightedPair<Float>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		RandomUtils.populateProbabilityMap(Float.class, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
	}
	
	public static void populateFloatProbabilityMap(LinkedList<WeightedPair<Float>> probabilityMap, ConfigurationSection nodeMap) {
		RandomUtils.populateProbabilityMap(Float.class, probabilityMap, nodeMap, 0, 0, 0);
	}

	public static <T extends Object> void populateProbabilityMap(Class<T> valueClass, LinkedList<WeightedPair<T>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
		Float currentThreshold = 0.0f;
		
		if (nodeMap != null) {
			Set<String> keys = nodeMap.getKeys(false);
			for (String key : keys) {
				// Kind of a hack, but the yaml parser doesn't like "." in a key.
				String value = nodeMap.getString(key);
				key = key.replace("^", ".");
				
				String[] pieces = value.split(",");
				if (pieces != null && pieces.length > 1) {
					currentThreshold += lerp(pieces, levelIndex, nextLevelIndex, distance);
				} else {
					currentThreshold += Float.parseFloat(value);
				}
				probabilityMap.add(new WeightedPair<T>(currentThreshold, key, valueClass));
			}
		}
	}
}
