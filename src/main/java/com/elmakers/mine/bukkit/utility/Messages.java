package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Messages implements com.elmakers.mine.bukkit.api.magic.Messages {
    private static String PARAMETER_PATTERN_STRING = "\\$([^ :]+)";
    private static Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_PATTERN_STRING);
    private static Random random = new Random();

    private Map<String, String> messageMap = new HashMap<String, String>();
    private Map<String, List<String>> randomized = new HashMap<String, List<String>>();
    private ConfigurationSection configuration = null;

    public Messages() {

    }

    public void load(ConfigurationSection messages) {
        configuration = messages;
        Collection<String> keys = messages.getKeys(true);
        for (String key : keys) {
            if (key.equals("randomized")) {
                ConfigurationSection randomSection = messages.getConfigurationSection(key);
                Set<String> randomKeys = randomSection.getKeys(false);
                for (String randomKey : randomKeys) {
                    randomized.put(randomKey, randomSection.getStringList(randomKey));
                }
            } else if (messages.isString(key)) {
                String value = messages.getString(key);
                value = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeHtml(value));
                messageMap.put(key, value);
            }
        }
    }

    public List<String> getAll(String path) {
        if (configuration == null) return new ArrayList<String>();
        return configuration.getStringList(path);
    }

    public void reset() {
        messageMap.clear();
    }

    public String get(String key, String defaultValue) {
        if (messageMap.containsKey(key)) {
            return messageMap.get(key);
        }
        if (defaultValue == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', defaultValue);
    }

    public String get(String key) {
        return get(key, key);
    }

    public String getParameterized(String key, String paramName, String paramValue) {
        return get(key, key).replace(paramName, paramValue);
    }

    public String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2) {
        return get(key, key).replace(paramName1, paramValue1).replace(paramName2, paramValue2);
    }

    public String getRandomized(String key) {
        if (!randomized.containsKey(key)) return null;
        List<String> options = randomized.get(key);
        if (options.size() == 0) return "";
        return options.get(random.nextInt(options.size()));
    }

    public String escape(String source) {
        Matcher matcher = PARAMETER_PATTERN.matcher(source);
        String result = source;
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key != null) {
                String randomized = getRandomized(key);
                if (randomized != null)
                {
                    result = result.replace("$" + key, randomized);
                }
            }
        }

        return result;
    }
    
    @Override
    public String describeItem(ItemStack item) {
        String displayName = null;
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            displayName = meta.getDisplayName();
            if ((displayName == null || displayName.isEmpty()) && meta instanceof BookMeta) {
                BookMeta book = (BookMeta)meta;
                displayName = book.getTitle();
            }
        }
        if (displayName == null || displayName.isEmpty()) {
            MaterialAndData material = new MaterialAndData(item);
            displayName = material.getName();
        }

        return displayName;
    }
    
    @Override
    public String describeCurrency(double amount) {
        VaultController vault = VaultController.getInstance();
        String formatted = vault.format(amount);
        if (!vault.hasEconomy()) {
            formatted =  get("costs.currency_amount").replace("$amount", formatted);
        }
        
        return formatted;
    }

    @Override
    public String getCurrency() {
        VaultController vault = VaultController.getInstance();
        if (vault.hasEconomy()) {
            return vault.getCurrency();
        }
        
        return get("costs.currency");
    }

    @Override
    public String getCurrencyPlural() {
        VaultController vault = VaultController.getInstance();
        if (vault.hasEconomy()) {
            return vault.getCurrencyPlural();
        }

        return get("costs.currency_plural");
    }
}
