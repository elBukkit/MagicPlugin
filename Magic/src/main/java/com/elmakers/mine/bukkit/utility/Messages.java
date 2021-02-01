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
import javax.annotation.Nonnull;
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

    public static DecimalFormat RANGE_FORMATTER = new DecimalFormat("0.#");
    public static DecimalFormat HOURS_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat MINUTES_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat SECONDS_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat MOMENT_MILLISECONDS_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat MOMENT_SECONDS_FORMATTER = new DecimalFormat("0.##");

    private Map<String, String> messageMap = new HashMap<>();
    private Map<String, List<String>> listMap = new HashMap<>();
    private Map<String, List<String>> randomized = new HashMap<>();

    private NumberFormat formatter = new DecimalFormat("#0.00");

    public Messages() {

    }

    public void load(ConfigurationSection messages) {
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
            } else if (messages.isList(key)) {
                listMap.put(key, messages.getStringList(key));
            }
        }
    }

    @Override
    public List<String> getAll(String path) {
        return listMap.get(path);
    }

    @Override
    @Nonnull
    public Collection<String> getAllKeys() {
        List<String> allKeys = new ArrayList<>();
        allKeys.addAll(listMap.keySet());
        allKeys.addAll(messageMap.keySet());
        return allKeys;
    }

    public void reset() {
        messageMap.clear();
        listMap.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return messageMap.containsKey(key) || listMap.containsKey(key);
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
        if (item == null) return "?";
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

        return get("costs.currency_singular");
    }

    @Override
    public String getCurrencyPlural() {
        VaultController vault = VaultController.getInstance();
        if (VaultController.hasEconomy()) {
            return vault.getCurrencyPlural();
        }

        return get("costs.currency");
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
            if (max != 1 && max != 0) {
                amount = amount / max;
            }
            String property = max == 0 ? getRomanLevelString((int)Math.ceil(amount)) : getRomanString(amount);
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

    private String getRomanLevelString(int level) {
        String roman = "";
        boolean negative = false;
        if (level < 0) {
            level = -level;
            negative = true;
        }

        if (level > 5) {
            roman = get("wand.enchantment_level_max");
        } else {
            if (level == 0) level = 1;
            roman = get("wand.enchantment_level_" + level);
        }
        if (negative) {
            roman = "-" + roman;
        }
        return roman;
    }

    private String getWithFallback(String key, String path, String fallbackPath) {
        if (fallbackPath == null || fallbackPath.isEmpty()) {
            return get(path + "." + key);
        }
        return get(path + "." + key, get(fallbackPath + "." + key));
    }

    @Override
    @Nonnull
    public String getTimeDescription(long time) {
        return getTimeDescription(time, "description", null);
    }

    @Override
    @Nonnull
    public String getTimeDescription(long time, @Nonnull String descriptionType) {
        return getTimeDescription(time, descriptionType, null);
    }

    @Override
    @Nonnull
    public String getTimeDescription(long time, @Nonnull String descriptionType, @Nullable String messagesPath) {
        if (time > 0) {
            double timeInSeconds = (double)time / 1000;
            if (timeInSeconds >= 60 * 60) {
                double hours = timeInSeconds / (60 * 60);
                if ((long)Math.floor(hours) == 1) {
                    return getWithFallback(descriptionType + "_hour", "time", messagesPath)
                        .replace("$hours", HOURS_FORMATTER.format(hours));
                }
                return getWithFallback(descriptionType + "_hours", "time", messagesPath)
                    .replace("$hours", HOURS_FORMATTER.format(hours));
            } else if (timeInSeconds >= 60) {
                double minutes = timeInSeconds / 60;
                if ((long)Math.floor(minutes) == 1) {
                    return getWithFallback(descriptionType + "_minute", "time", messagesPath)
                        .replace("$minutes", MINUTES_FORMATTER.format(minutes));
                }
                return getWithFallback(descriptionType + "_minutes", "time", messagesPath)
                    .replace("$minutes", MINUTES_FORMATTER.format(minutes));
            } else if (timeInSeconds >= 2) {
                return getWithFallback(descriptionType + "_seconds", "time", messagesPath)
                    .replace("$seconds", SECONDS_FORMATTER.format(timeInSeconds));
            } else if (timeInSeconds >= 1) {
                return getWithFallback(descriptionType + "_second", "time", messagesPath)
                    .replace("$seconds", SECONDS_FORMATTER.format(timeInSeconds));
            } else {
                String timeDescription = getWithFallback(descriptionType + "_moment", "time", messagesPath);
                timeDescription = timeDescription
                    .replace("$milliseconds", MOMENT_MILLISECONDS_FORMATTER.format(timeInSeconds * 1000))
                    .replace("$seconds", MOMENT_SECONDS_FORMATTER.format(timeInSeconds));
                return timeDescription;
            }
        }
        return "0";
    }

    @Override
    @Nonnull
    public String getRangeDescription(double range, @Nonnull String messagesKey) {
        return get(messagesKey).replace("$range", RANGE_FORMATTER.format(range));
    }
}
