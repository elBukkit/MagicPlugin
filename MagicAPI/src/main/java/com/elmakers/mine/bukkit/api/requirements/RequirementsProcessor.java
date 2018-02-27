package com.elmakers.mine.bukkit.api.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Implement this and register in LoadEvent to provide custom requirements.
 */
public interface RequirementsProcessor {
    /**
     * Check if a player fulfills the target requirement. Requirements are defined ad-hoc on spell configurations,
     * Selector configurations and perhaps other configurations in the future.
     * 
     * There is no central definition of all known requirements, and processors need not register them in advance.
     * 
     * @param player The player being queried
     * @param requirementType The "type" field from the requirement configuration. This should generally be a top-level
     *                        grouping used for quick determination about whether this processor cares about the given
     *                        requirement.
     * @param configuration The full configuration section provided in the requirement.
     * @return true if the player has the requirement
     */
    boolean checkRequirement(Player player, String requirementType, ConfigurationSection configuration);

    /**
     * This will be called when a requirement check has failed, in order to display to the player what they are missing.
     * This will generally be added to item lore for display in a GUI.
     * 
     * @param player The player being queried
     * @param requirementType The "type" field from the requirement configuration.
     * @param configuration The full configuration section provided in the requirement.
     * @return a player-readable description of what this requirement represents.
     */
    String getRequirementDescription(Player player, String requirementType, ConfigurationSection configuration);
}
