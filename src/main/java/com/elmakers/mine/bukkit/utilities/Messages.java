package com.elmakers.mine.bukkit.utilities;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Messages {
	private static final String                        messagesFileName               = "messages.yml";
	private static final String                        messagesFileNameDefaults       = "messages.defaults.yml";

	public static Map<String, String> messageMap = new HashMap<String, String>();
	
	public static void loadProperties(Configuration root) {
		root.load();
		
		ConfigurationNode messages = root.getNode("messages");
		if (messages == null) return;

		Map<String, Object> allMap = messages.getAll();
		for (Entry<String, Object> entry : allMap.entrySet()) {
			messageMap.put(entry.getKey(), (String)entry.getValue());
		}
	}

	public static void load(Plugin plugin) {
		File dataFolder = plugin.getDataFolder();
		File oldDefaults = new File(dataFolder, messagesFileNameDefaults);
		oldDefaults.delete();
		plugin.getLogger().info("Overwriting file " + messagesFileNameDefaults);
		plugin.saveResource(messagesFileNameDefaults, false);
		
		// Load default configuration first, then override with custom.
		plugin.getLogger().info("Loading default localizations from " + messagesFileNameDefaults);
		loadProperties(plugin.getResource(messagesFileNameDefaults));
		File propertiesFile = new File(dataFolder, messagesFileName);
		if (propertiesFile.exists())
		{
			plugin.getLogger().info("Overriding default localizations using " + propertiesFile);
			loadProperties(propertiesFile);
		}
	}
	
	private static void loadProperties(File propertiesFile)
	{
		loadProperties(new Configuration(propertiesFile));
	}
	
	private static void loadProperties(InputStream properties)
	{
		loadProperties(new Configuration(properties));
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
