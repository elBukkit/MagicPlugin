package com.elmakers.mine.bukkit.utilities;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class RandomUtils {
	private final static Random random = new Random();
	
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
}
