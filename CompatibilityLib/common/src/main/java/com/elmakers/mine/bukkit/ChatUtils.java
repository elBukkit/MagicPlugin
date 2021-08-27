package com.elmakers.mine.bukkit;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.Messages;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ChatUtils {
    // Remove some standard java punctuation, plus the unicode range from "Number Forms" to "Miscellaneous symbols"
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\s0-9\\p{Punct}\\p{IsPunctuation}\u2150-\u2BFF\uE000-\uFFFF]"); // Sorry for the unicode escapes
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

    protected static void getSimpleMessage(Map<String,Object> mapped, StringBuilder plainMessage, boolean onlyText) {
        for (Map.Entry<String,Object> entry : mapped.entrySet()) {
            if (entry.getKey().equals("text")) {
                plainMessage.append(entry.getValue());
            } else if (entry.getKey().equals("extra")) {
                Object rawExtra = entry.getValue();
                if (rawExtra instanceof List) {
                    List<Map<String, Object>> mapList = (List<Map<String, Object>>)rawExtra;
                    for (Map<String, Object> child : mapList) {
                        getSimpleMessage(child, plainMessage, onlyText);
                    }
                }
            } else if (entry.getKey().equals("keybind")) {
                String key = entry.getValue().toString().replace("key.", "");
                if (messages != null) {
                    key = messages.get("keybind." + key, key);
                }
                plainMessage.append(key);
            } else if (onlyText) {
                continue;
            } else if (entry.getKey().equals("color")) {
                String colorKey = entry.getValue().toString();
                try {
                    ChatColor color = ChatColor.valueOf(colorKey.toUpperCase());
                    plainMessage.append(color);
                } catch (Exception ignore) {
                }
            } else if (entry.getKey().equals("clickEvent")) {
                Map<String,Object> properties = (Map<String,Object>)entry.getValue();
                if (properties.containsKey("action") && properties.containsKey("value")) {
                    String value = properties.get("value").toString();
                    String action = properties.get("action").toString();
                    switch (action) {
                        case "open_url":
                        case "run_command":
                        case "suggest_command":
                            plainMessage.append(ChatColor.RESET + " " + ChatColor.GRAY + value);
                            break;
                    }
                }
            }
        }
    }

    public static String getSimpleMessage(String containsJson) {
        return getSimpleMessage(containsJson, false);
    }

    public static String getSimpleMessage(String containsJson, boolean onlyText) {
        String[] components = getComponents(containsJson);
        StringBuilder plainMessage = new StringBuilder();
        for (String component : components) {
            if (component.startsWith("{")) {
                try {
                    JsonReader reader = new JsonReader(new StringReader(component));
                    reader.setLenient(true);
                    Map<String, Object> mapped = getGson().fromJson(reader, Map.class);
                    getSimpleMessage(mapped, plainMessage, onlyText);
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

    public static String[] getWords(String text) {
        return PUNCTUATION_PATTERN.split(text);
    }

    public static boolean hasJSON(String text) {
        return text.contains("`{");
    }

    public static boolean isDefaultFont(String font) {
        return font == null || font.isEmpty() || font.equals("default");
    }

    public static void sendToConsoleSender(String message, Logger logger) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (sender == null) {
            logger.info(ChatColor.stripColor(message));
        } else {
            sender.sendMessage(message);
        }
    }
}
