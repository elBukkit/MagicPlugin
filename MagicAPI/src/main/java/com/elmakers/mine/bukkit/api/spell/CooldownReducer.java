package com.elmakers.mine.bukkit.api.spell;

/**
 * This interface may be used by a spell to reduce or remove cooldowns.
 */
public interface CooldownReducer {
    /**
     * Get the percent cooldown reduction, from 0.0 to 1.0
     * @return The percent cooldown reduction.
     */
    float getCooldownReduction();

    /**
     * Check if cooldowns should be bypassed completely.
     * @return True if cooldowns are disabled
     */
    boolean isCooldownFree();
}
