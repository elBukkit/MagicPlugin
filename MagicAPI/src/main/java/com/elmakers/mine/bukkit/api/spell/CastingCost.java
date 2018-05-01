package com.elmakers.mine.bukkit.api.spell;

import com.elmakers.mine.bukkit.api.item.Cost;

/**
 * This reprsents a cost required to cast a Spell.
 *
 * <p>A Spell may have one or more (or no) CastingCost
 * records assigned to it.
 *
 * <p>A CastingCost can be XP (or "Mana" when provided by a wand)
 * or Material (reagents) as costs, or a combination of both.
 */
public interface CastingCost extends Cost {
    /**
     * Returns the raw XP cost.
     *
     * @return The raw XP cost, without reduction.
     */
    @Deprecated
    int getXP();

    /**
     * Returns the XP amount to deduct
     *
     * @param reducer The CostReducer to use to calculate costs
     * @return The XP amount cost
     */
    @Deprecated
    int getXP(CostReducer reducer);

    /**
     * Returns the raw mana cost.
     *
     * @return The raw mana cost, without reduction.
     */
    int getMana();

    /**
     * Returns the Mana amount to deduct
     *
     * @param reducer The CostReducer to use to calculate costs
     * @return The Mana amount cost
     */
    int getMana(CostReducer reducer);

    /**
     * Check to see if a given spell cast should succeed, given that
     * it has any required costs.
     *
     * @param spell the spell being cast
     * @return true if the caster of the spell has this cost
     */
    boolean has(Spell spell);

    /**
     * Use this cost, taking it from the caster of the target spell
     *
     * @param spell the spell being cast
     */
    void use(Spell spell);
}
