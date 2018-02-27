package com.elmakers.mine.bukkit.api.requirements;

import com.elmakers.mine.bukkit.api.magic.Mage;
import org.bukkit.configuration.ConfigurationSection;

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
     * @param mage The mage being queried
     * @param configuration The full configuration section provided in the requirement.
     * @return true if the player has the requirement
     */
    boolean checkRequirement(Mage mage, ConfigurationSection configuration);

    /**
     * This will be called when a requirement check has failed, in order to display to the player what they are missing.
     * This will generally be added to item lore for display in a GUI.
     * 
     * @param mage The mage being queried
     * @param configuration The full configuration section provided in the requirement.
     * @return a player-readable description of what this requirement represents.
     */
    String getRequirementDescription(Mage mage, ConfigurationSection configuration);
}
