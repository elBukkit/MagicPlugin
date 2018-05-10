package com.elmakers.mine.bukkit.utility;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;

public class Messages implements com.elmakers.mine.bukkit.api.magic.Messages {
    private static String PARAMETER_PATTERN_STRING = "\\$([a-zA-Z0-9]+)";
    private static Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_PATTERN_STRING);
    private static Random random = new Random();

    private Map<String, String> messageMap = new HashMap<>();
    private Map<String, List<String>> randomized = new HashMap<>();
    private ConfigurationSection configuration = null;

    private NumberFormat formatter = new DecimalFormat("#0.00");

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

    @Override
    public List<String> getAll(String path) {
        if (configuration == null) return new ArrayList<>();
        return configuration.getStringList(path);
    }

    public void reset() {
        messageMap.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return messageMap.containsKey(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        if (messageMap.containsKey(key)) {
            return messageMap.get(key);
        }
        if (defaultValue == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', defaultValue);
    }

    @Override
    public String get(String key) {
        return get(key, key);
    }

    @Override
    public String getParameterized(String key, String paramName, String paramValue) {
        return get(key, key).replace(paramName, paramValue);
    }

    @Override
    public String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2) {
        return get(key, key).replace(paramName1, paramValue1).replace(paramName2, paramValue2);
    }

    @Nullable
    @Override
    public String getRandomized(String key) {
        if (!randomized.containsKey(key)) return null;
        List<String> options = randomized.get(key);
        if (options.size() == 0) return "";
        return options.get(random.nextInt(options.size()));
    }

    @Override
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
        if (vault == null) return Integer.toString((int)amount);
        String formatted = vault.format(amount);
        if (!VaultController.hasEconomy()) {
            formatted =  get("costs.currency_amount").replace("$amount", formatted);
        }

        return formatted;
    }

    @Override
    public String getCurrency() {
        VaultController vault = VaultController.getInstance();
        if (VaultController.hasEconomy()) {
            return vault.getCurrency();
        }

        return get("costs.currency");
    }

    @Override
    public String getCurrencyPlural() {
        VaultController vault = VaultController.getInstance();
        if (VaultController.hasEconomy()) {
            return vault.getCurrencyPlural();
        }

        return get("costs.currency_plural");
    }

    @Override
    public String formatList(String basePath, Collection<String> nodes, String nameKey) {
        StringBuilder buffer = new StringBuilder();
        for (String node : nodes) {
            if (buffer.length() != 0) {
                buffer.append(", ");
            }
            String path = node;
            if (basePath != null) {
                path = basePath + "." + path;
            }
            if (nameKey != null) {
                path = path + "." + nameKey;
            }
            node = get(path, node);
            buffer.append(node);
        }
        return buffer.toString();
    }

    @Override
    public String getLevelString(String templateName, float amount)
    {
        return getLevelString(templateName, amount, 1);
    }

    @Override
    public String getLevelString(String templateName, float amount, float max) {
        String templateString = get(templateName, "");
        return formatLevelString(templateString, amount, max);
    }

    @Override
    public String getPropertyString(String templateName, float amount, float max, String propertyTemplateName) {
        String templateString = get(templateName, "");
        String propertyTemplateString = get(propertyTemplateName, "");
        return formatPropertyString(templateString, amount, max, propertyTemplateString);
    }

    @Override
    public String formatLevelString(String message, float amount)
    {
        return formatLevelString(message, amount, 1);
    }

    @Override
    public String formatLevelString(String message, float amount, float max)
    {
        return formatPropertyString(message, amount, max, null);
    }

    @Override
    public String formatPropertyString(String message, float amount, float max, @Nullable String propertyTemplate)
    {
        if (message.contains("$roman")) {
            if (max != 1) {
                amount = amount / max;
            }
            String property = getRomanString(amount);
            if (propertyTemplate != null) {
                property = propertyTemplate.replace("$property", property);
            }
            message = message.replace("$roman",property);
        }
        String valueString = Math.floor(amount) == amount ? Integer.toString((int)Math.floor(amount)) : formatter.format(amount);
        if (propertyTemplate != null) {
            valueString = propertyTemplate.replace("$property", valueString);
        }
        String amountString = Integer.toString(Math.round(amount));
        if (propertyTemplate != null) {
            amountString = propertyTemplate.replace("$property", amountString);
        }
        String percentString = Integer.toString((int)Math.round(100.0 * amount));
        if (propertyTemplate != null) {
            percentString = propertyTemplate.replace("$property", percentString);
        }
        return message.replace("$amount", amountString)
                .replace("$value", valueString)
                .replace("$percent", percentString);
    }

    @Override
    public String getPercentageString(String templateName, float amount)
    {
        String templateString = get(templateName, "");
        return templateString.replace("$amount", Integer.toString((int)(amount * 100)));
    }

    private String getRomanString(float amount) {
        String roman = "";
        boolean negative = false;
        if (amount < 0) {
            amount = -amount;
            negative = true;
        }

        if (amount > 1) {
            roman = get("wand.enchantment_level_max");
        } else if (amount > 0.8f) {
            roman = get("wand.enchantment_level_5");
        } else if (amount > 0.6f) {
            roman = get("wand.enchantment_level_4");
        } else if (amount > 0.4f) {
            roman = get("wand.enchantment_level_3");
        } else if (amount > 0.2f) {
            roman = get("wand.enchantment_level_2");
        } else {
            roman = get("wand.enchantment_level_1");
        }
        if (negative) {
            roman = "-" + roman;
        }
        return roman;
    }
}
