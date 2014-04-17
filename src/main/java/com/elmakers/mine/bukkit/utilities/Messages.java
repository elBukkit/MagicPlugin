package com.elmakers.mine.bukkit.utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;


public class Messages {
	public static Map<String, String> messageMap = new HashMap<String, String>();
	
	public static void load(ConfigurationSection messages) {
		Collection<String> keys = messages.getKeys(true);
		for (String key : keys) {
			messageMap.put(key, messages.getString(key));
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
