package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;

public interface MagicConfigurable extends MagicProperties {
    /**
     * Transfer all properties from a configuration to this store, overwriting any
     * values already present in the store.
     *
     * @param configuration The configuration to copy.
     */
    void configure(@Nonnull ConfigurationSection configuration);

    /**
     * Configure a single property.
     *
     * <p>Do not use this to remove properties, use removeProperty instead.
     *
     * <p>Do not use this for setting a Map or ConfigurationSection, nested properties
     * should use the above configure method.
     *
     * @param key The property to configure.
     * @param value The new property value.
     */
    void configure(@Nonnull String key, @Nonnull Object value);

    /**
     * Transfer properties from a configuration to this store, following a set of
     * upgrade rules:
     *
     * <ol>
     * <li>String values are replaced unless they match with a case-insensitve comparison
     * <li>Numeric values are only replaced if they are greater than the ones present already in the store
     * <li>Certain other special rules apply to specific properties, such as spells are
     * merged.
     * </ol>
     *
     * <p>Anything else is copied over directly, overwriting what is in the store.
     *
     * @param configuration The configuration to copy into this store
     * @return true if any property was changed, false means the upgrade was rejected.
     */
    boolean upgrade(@Nonnull ConfigurationSection configuration);

    /**
     * Upgrade a single property.
     *
     * <p>Do not use this to remove properties, use removeProperty instead.
     *
     * @param key The property to upgrade.
     * @param value The new value to set, if it is an improvement over the current value.
     * @return true if the property was changed, false means the upgrade was rejected.
     */
    boolean upgrade(@Nonnull String key, @Nonnull Object value);

    /**
     * Remove a property from this store.
     *
     * <p>If this property is inherited, it cannot be removed.
     * If this property exists but an inherited value also exists, the property will now have the inherited value.
     *
     * @param key The key to remove
     * @return true if the property existed and was removed.
     */
    boolean removeProperty(String key);

    /**
     * Retrieve the storage associated with a given type
     */
    MagicConfigurable getStorage(MagicPropertyType storageType);
}
