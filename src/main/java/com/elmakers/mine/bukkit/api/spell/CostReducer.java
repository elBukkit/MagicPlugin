package com.elmakers.mine.bukkit.api.spell;

/**
 * This interface may be used by a CastingCost to 
 * reduce costs, or to change the display mode from XP to Mana.
 * 
 * A CostReducer may be a Spell, Wand or Mage (or anything
 * else that cares to implement this interface)
 */
public interface CostReducer {
    /**
     * Get the percent cost reduction, from 0.0 to 1.0
     * @return The percent cost reduction.
     */
    public float getCostReduction();

    /**
     * Get whether or not the display should use Mana
     * instead of XP.
     *
     * @return True if a Mana display should be used.
     */
    public boolean usesMana();
}
