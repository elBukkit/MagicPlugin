package com.elmakers.mine.bukkit.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Messages {
	public static Map<String, String> messageMap = new HashMap<String, String>();
	
	public static void load(ConfigurationNode messages) {
		Map<String, Object> allMap = messages.getAll();
		for (Entry<String, Object> entry : allMap.entrySet()) {
			messageMap.put(entry.getKey(), (String)entry.getValue());
		}
	}
	
	public static void reset() {
		messageMap.clear();
	}
	
	public static String get(String key, String defaultValue) {
		return messageMap.containsKey(key) ? messageMap.get(key) : defaultValue;
	}
	
	public static String get(String key) {
		return get(key, key);
	}
}
