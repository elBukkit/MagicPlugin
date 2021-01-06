package com.elmakers.mine.bukkit.api.requirements;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;

/**
 * Replaces RequirementsProcessor for easier registration via MageController.register()
 */
public interface RequirementsProvider extends RequirementsProcessor, MagicProvider {
    /**
     * Used in CheckRequirements and other configurations for specifying different requirements types.
     * Must be a unique key.
     * @return The unique key for this provider
     */
    @Nonnull
    String getKey();
}
