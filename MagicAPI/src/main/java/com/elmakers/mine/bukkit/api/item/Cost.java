package com.elmakers.mine.bukkit.api.item;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an abstract cost that may be one of several types:
 * 
 * mana, XP, SP, Vault currency, or an item stack.
 */
public interface Cost {

    /**
     * Whether or not this record has any costs associated with it,
     * after being reduced
     *
     * @param reducer The CostReducer to use for calculating costs
     * @return False if this has any non-zero costs.
     */
    boolean isEmpty(CostReducer reducer);

    /**
     * Whether or not this record has any costs associated with it,
     * after being reduced
     *
     * @param mage The mage to check for costs
     * @param caster A optional caster, to be used for mana costs
     * @param reducer An optional scale for costs
     * @return True if this has any non-zero costs.
     */
    boolean has(Mage mage, CasterProperties caster, CostReducer reducer);

    /**
     * Whether or not this record has any costs associated with it,
     * after being reduced
     *
     * @param mage The mage to check for costs
     * @param wand A optional wand, to be used for mana costs
     * @param reducer An optional scale for costs
     * @return True if this has any non-zero costs.
     */
    boolean has(Mage mage, Wand wand, CostReducer reducer);
    
    /**
     * Whether or not this record has any costs associated with it,
     * after being reduced
     *
     * @param mage The mage to check for costs
     * @return True if this has any non-zero costs.
     */
    boolean has(Mage mage);

    /**
     * Deduct these costs from the specified Mage
     *
     * @param mage The mage to check for costs
     * @param caster A optional caster, to be used for mana costs
     * @param reducer An optional scale for costs
     */
    void deduct(Mage mage, CasterProperties caster, CostReducer reducer);

    /**
     * Deduct these costs from the specified Mage
     *
     * @param mage The mage to check for costs
     * @param wand A optional wand, to be used for mana costs
     * @param reducer An optional scale for costs
     */
    void deduct(Mage mage, Wand wand, CostReducer reducer);

    /**
     * Deduct these costs from the specified Mage
     *
     * @param mage The mage to check for costs
     * @return False if deducting costs failed
     */
    void deduct(Mage mage);

    /**
     * Get a human-readable description of this cost.
     *
     * For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * This does not include the amount, and only the label - e.g. "Bread" or "Mana" or "XP".
     *
     * @param messages The Messages class for looking up localizations
     * @param reducer An optional scale for costs
     * @return A printable String to display this casting cost to a Player.
     */
    String getDescription(Messages messages, CostReducer reducer);

    /**
     * Get a human-readable description of this cost.
     *
     * For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * This does not include the amount, and only the label - e.g. "Bread" or "Mana" or "XP".
     *
     * @param messages The Messages class for looking up localizations
     * @return A printable String to display this casting cost to a Player.
     */
    String getDescription(Messages messages);

    /**
     * Get a human-readable description of this cost.
     *
     * For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * This includes the amount as well as the label - e.g. "2 Bread" or "30 Mana" or "50 XP".
     *
     * @param messages The Messages class for looking up localizations
     * @param reducer An optional scale for costs
     * @return A printable String to display this casting cost to a Player.
     */
    String getFullDescription(Messages messages, CostReducer reducer);
    
    /**
     * Get a human-readable description of this cost.
     *
     * For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * This includes the amount as well as the label - e.g. "2 Bread" or "30 Mana" or "50 XP".
     * 
     * @param messages The Messages class for looking up localizations
     * @return A printable String to display this casting cost to a Player.
     */
    String getFullDescription(Messages messages);

    /**
     * Get a copy of the item represented by this cost.
     * 
     * This will return null for non-item costs.
     * 
     * @return The item cost, or null if none.
     */
    ItemStack getItemStack();

    /**
     * Check to see if this Cost is an ItemStack
     * 
     * @return true if this cost is an item
     */
    boolean isItem();

    /**
     * Multiply this cost by a scale value.
     *
     * @param scale The cost multiplier.
     */
    void scale(double scale);
}
