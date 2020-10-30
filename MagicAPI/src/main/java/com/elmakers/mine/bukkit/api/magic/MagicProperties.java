package com.elmakers.mine.bukkit.api.magic;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;

public interface MagicProperties {
    /**
     * Check to see if this property store contains a specific property.
     *
     * <p>If this is an inherited store (such as a Wand to a WandTemplate), inherited
     * properties are checked as well.
     *
     * @param key The key to check
     * @return true if this store has the specified key.
     */
    boolean hasProperty(@Nonnull String key);

    /**
     * Return the raw value of a key in the store, or null if the key is not
     * present.
     *
     * @param key The key to retrieve
     * @return The raw value stored for the given key, or null if not present.
     */
    @Nullable
    Object getProperty(@Nonnull String key);

    /**
     * Get a property only if it is of a specific type.
     *
     * @param key The key to retrieve
     * @param type The class the value is expected to be
     * @param <T> The property type.
     * @return An Optional wrapper for the value
     */
    @Nullable
    <T> T getProperty(@Nonnull String key, @Nonnull Class<T> type);

    /**
     * Get a property, specifying a default value.
     *
     * @param key The key to retrieve
     * @param defaultValue The value to return if the key is not present in the store or is not the correct type.
     * @param <T> The property type.
     * @return The value, if present and of the correct type, otherwise returns defaultValue
     */
    @Nonnull
    <T> T getProperty(@Nonnull String key, @Nonnull T defaultValue);

    /**
     * Print all properties to a CommandSender in a human-readable format.
     *
     * @param sender Where to print properties.
     */
    void describe(CommandSender sender);

    /**
     * Print all properties except those in a specific set.
     *
     * <p>Useful for differentiating between inherited and non-inherited properties.
     *
     * @param sender Where to print properties.
     * @param ignoreProperties A set of properties to ignore
     */
    void describe(CommandSender sender, @Nullable Set<String> ignoreProperties);

    /**
     * Check to see if there is any data here at all.
     *
     * @return False if there are any keys set on this property holder directly.
     */
    boolean isEmpty();

    double getDouble(String key, double defaultValue);
    double getDouble(String key);
    float getFloat(String key, float defaultValue);
    float getFloat(String key);
    int getInt(String key, int defaultValue);
    int getInt(String key);
    long getLong(String key, long defaultValue);
    long getLong(String key);
    boolean getBoolean(String key, boolean defaultValue);
    boolean getBoolean(String key);
    String getString(String key, String defaultValue);
    String getString(String key);
    @Nullable
    List<String> getStringList(String key);
}
