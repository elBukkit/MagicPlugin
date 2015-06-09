package com.elmakers.mine.bukkit.api.spell;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Messages;

/**
 * This reprsents a cost required to cast a Spell.
 * 
 * A Spell may have one or more (or no) CastingCost
 * records assigned to it.
 * 
 * A CastingCost can be XP (or "Mana" when provided by a wand)
 * or Material (reagents) as costs, or a combination of both.
 * 
 */
public interface CastingCost {
    /**
     * Whether or not this record has any costs associated with it,
     * after being reduced
     *
     * @param reducer The CostReducer to use for calculating costs
     * @return True if this has any non-zero costs.
     */
    public boolean hasCosts(CostReducer reducer);

    /**
     * Get a human-readable description of this cost.
     *
     * For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * This does not include the amount, and only the label - e.g. "Bread" or "Mana" or "XP".
     *
     * @param reducer The CostReducer to use to calculate costs and determine display type
     * @return A printable String to display this casting cost to a Player.
     */
    public String getDescription(Messages messages, CostReducer reducer);

    /**
     * Get a human-readable description of this cost.
     *
     * For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * This includes the amount as well as the label - e.g. "2 Bread" or "30 Mana" or "50 XP".
     *
     * @param reducer The CostReducer to use to calculate costs and determine display type
     * @return A printable String to display this casting cost to a Player.
     */
    public String getFullDescription(Messages messages, CostReducer reducer);

    /**
     * Returns the raw XP cost.
     *
     * @return The raw XP cost, without reduction.
     */
    public int getXP();

    /**
     * Returns the raw mana cost.
     *
     * @return The raw mana cost, without reduction.
     */
    public int getMana();

    /**
     * Returns the raw item amount cost.
     *
     * @return The raw item amount cost, without reduction.
     */
    public int getAmount();

    /**
     * Returns the XP amount to deduct
     *
     * @param reducer The CostReducer to use to calculate costs
     * @return The XP amount cost
     */
    public int getXP(CostReducer reducer);

    /**
     * Returns the Mana amount to deduct
     *
     * @param reducer The CostReducer to use to calculate costs
     * @return The Mana amount cost
     */
    public float getMana(CostReducer reducer);

    /**
     * Returns the item amount to deduct
     *
     * @param reducer The CostReducer to use to calculate costs
     * @return The item amount cost
     */
    public int getAmount(CostReducer reducer);

    /**
     * Get the item, if any, associated with this cost.
     *
     * @return The item to consume, or null if none.
     */
    public MaterialAndData getMaterial();
}
