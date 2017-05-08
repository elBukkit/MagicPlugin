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
    void configure(@Nonnull  ConfigurationSection configuration);

    /**
     * Transfer properties from a configuration to this store, following a set of
     * upgrade rules:
     *
     * 1. String values are replaced unless they match with a case-insensitve comparison
     * 2. Numeric values are only replaced if they are greater than the ones present already in the store
     * 3. Certain other special rules apply to specific properties, such as spells are
     * merged.
     *
     * Anything else is copied over directly, overwriting what is in the store.
     *
     * @param configuration The configuration to copy into this store
     * @return true if any property was changed, false means the upgrade was rejected.
     */
    boolean upgrade(@Nonnull ConfigurationSection configuration);

    /**
     * Remove a property from this store.
     *
     * If this property is inherited, it cannot be removed.
     * If this property exists but an inherited value also exists, the property will now have the inherited value.
     *
     * @param key The key to remove
     * @return true if the property existed and was removed.
     */
    boolean removeProperty(String key);
}
