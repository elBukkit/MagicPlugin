package com.elmakers.mine.bukkit.api.requirements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.magic.MageContext;

/**
 * Implement this and register in PreLoadEvent to provide custom requirements.
 * Use RequirementsProvider instead for simpler registration.
 */
public interface RequirementsProcessor {
    /**
     * Check if a player fulfills the target requirement. Requirements are defined ad-hoc on spell configurations,
     * Selector configurations and perhaps other configurations in the future.
     *
     * <p>There is no central definition of all known requirements, and processors need not register them in advance.
     *
     * @param context The context under which this query is taking place
     * @param requirement The requirement being checked
     * @return true if the player has the requirement
     */
    boolean checkRequirement(@Nonnull MageContext context, @Nonnull Requirement requirement);

    /**
     * This will be called when a requirement check has failed, in order to display to the player what they are missing.
     * This will generally be added to item lore for display in a GUI.
     *
     * @param context The context under which this requirement failed
     * @param requirement The requirement that failed
     * @return a player-readable description of what this requirement represents.
     */
    @Nullable
    String getRequirementDescription(@Nonnull MageContext context, @Nonnull Requirement requirement);
}
