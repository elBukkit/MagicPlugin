package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

public interface Messages {
    boolean containsKey(String key);
    String get(String key);
    String get(String key, String defaultValue);
    @Nullable
    String getRandomized(String key);
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

    /**
     * Get a level string that applies a separate template to the value, filling the value into the $property variable.
     * The escaped property template is then applied to the level string via the normal $roman,$value,$percentage,$amount
     * variables.
     *
     * @param templateName The key of the string template, such as "protection.fall", which would have an entry such as
     *                     "Protection $roman"
     * @param amount The amount of the property to print
     * @param max The max value of the property to print, used in "roman" mode
     * @param propertyTemplateName the template to use for formatting the amount, such as "properties.stacked_negative,
     *                             which would have an entry such as "+$property".
     *                             This is most often used to differentiate between positive and negative values.
     * @return
     */
    String getPropertyString(String templateName, float amount, float max, String propertyTemplateName);

    String formatLevelString(String message, float amount);
    String formatLevelString(String message, float amount, float max);

    /**
     * Similar to getPropertyString, but this one doesn't do the message lookup for you. You pass in messages directly
     * to be escaped.
     *
     * @param template The string to escape, such as "Protection $roman"
     * @param amount The amount of the property to print
     * @param max The max value of the property to print, used in "roman" mode
     * @param propertyTemplate The string to use for property formatting, such as "+&4$property"
     * @return
     */
    String formatPropertyString(String template, float amount, float max, String propertyTemplate);

    @Nonnull
    String getTimeDescription(long time, @Nonnull String descriptionType);
    @Nonnull
    String getTimeDescription(long time, @Nonnull String descriptionType, @Nullable String messagesPath);

    @Nonnull
    String getRangeDescription(double range, @Nonnull String messagesKey);
}
