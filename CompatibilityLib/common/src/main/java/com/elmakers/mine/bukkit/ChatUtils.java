package com.elmakers.mine.bukkit;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.api.magic.Messages;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ChatUtils {
    private static Messages messages;
    private static Logger logger;
    private static Gson gson;

    public static void initialize(Messages messages, Logger logger) {
        ChatUtils.messages = messages;
        ChatUtils.logger = logger;
    }

    protected static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    protected static void getSimpleMessage(Map<String,Object> mapped, StringBuilder plainMessage) {
        for (Map.Entry<String,Object> entry : mapped.entrySet()) {
            if (entry.getKey().equals("color")) {
                String colorKey = entry.getValue().toString();
                try {
                    ChatColor color = ChatColor.valueOf(colorKey.toUpperCase());
                    plainMessage.append(color);
                } catch (Exception ex) {
                    if (logger != null) {
                        logger.warning("Invalid color in json message: " + colorKey);
                    }
                }
            } if (entry.getKey().equals("text")) {
                plainMessage.append(entry.getValue());
            } else if (entry.getKey().equals("keybind")) {
                String key = entry.getValue().toString().replace("key.", "");
                if (messages != null) {
                    key = messages.get("keybind." + key, key);
                }
                plainMessage.append(key);
            } else if (entry.getKey().equals("extra")) {
                Object rawExtra = entry.getValue();
                if (rawExtra instanceof List) {
                    List<Map<String, Object>> mapList = (List<Map<String, Object>>)rawExtra;
                    for (Map<String, Object> child : mapList) {
                        getSimpleMessage(child, plainMessage);
                    }
                }
            }
        }
    }

    public static String getSimpleMessage(String containsJson) {
        String[] components = getComponents(containsJson);
        StringBuilder plainMessage = new StringBuilder();
        for (String component : components) {
            if (component.startsWith("{")) {
                try {
                    JsonReader reader = new JsonReader(new StringReader(component));
                    reader.setLenient(true);
                    Map<String, Object> mapped = getGson().fromJson(reader, Map.class);
                    getSimpleMessage(mapped, plainMessage);
                } catch (Exception ex) {
                    plainMessage.append(component);
                }
            } else {
                plainMessage.append(component);
            }
        }
        return plainMessage.toString();
    }

    public static String[] getComponents(String containsJson) {
        return StringUtils.split(containsJson, "`");
    }
}
