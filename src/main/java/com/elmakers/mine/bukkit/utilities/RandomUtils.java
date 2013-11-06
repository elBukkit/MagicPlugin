package com.elmakers.mine.bukkit.utilities;

import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

public class RandomUtils {
	private final static Random random = new Random();
	
	public static <T extends Object> T weightedRandom(TreeMap<Float, T> weightMap) {
		if (weightMap.size() == 0) return null;
		
		Float maxWeight = weightMap.lastKey();
		Float selectedWeight = random.nextFloat() * maxWeight;
		for (Entry<Float, T> entry : weightMap.entrySet()) {
			if (selectedWeight < entry.getKey()) {
				return entry.getValue();
			}
		}
		
		return weightMap.lastEntry().getValue();
	}
}
