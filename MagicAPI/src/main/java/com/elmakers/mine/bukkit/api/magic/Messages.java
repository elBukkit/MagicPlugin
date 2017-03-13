package com.elmakers.mine.bukkit.api.magic;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public interface Messages {
    String get(String key);
    String getRandomized(String key);
    String get(String key, String defaultValue);
    List<String> getAll(String path);
    String getParameterized(String key, String paramName, String paramValue);
    String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2);
    String escape(String source);
    String describeItem(ItemStack item);
    String describeCurrency(double amount);
    String getCurrency();
    String getCurrencyPlural();
    String formatList(String basePath, Collection<String> nodes, String nameKey);
    String getLevelString(String templateName, float amount);
    String getLevelString(String templateName, float amount, float max);
    String getPercentageString(String templateName, float amount);
}
