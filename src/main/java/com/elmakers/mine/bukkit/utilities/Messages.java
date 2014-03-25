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
	
	public static String getParameterized(String key, String paramName, String paramValue) {
		return get(key, key).replace(paramName, paramValue);
	}
	
	public static String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2) {
		return get(key, key).replace(paramName1, paramValue1).replace(paramName2, paramValue2);
	}
}
