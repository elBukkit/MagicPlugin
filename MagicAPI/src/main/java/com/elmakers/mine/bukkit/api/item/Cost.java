package com.elmakers.mine.bukkit.api.item;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.wand.Wand;

/**
 * Represents an abstract cost that may be one of several types:
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
     * Whether or not a mage has the required costs.
     *
     * @param mage The mage to check for costs
     * @param caster A optional caster, to be used for mana costs
     * @param reducer An optional scale for costs
     * @return True if the mage has these costs.
     */
    boolean has(Mage mage, CasterProperties caster, CostReducer reducer);

    /**
     * Whether or not a mage has the required costs.
     *
     * @param mage The mage to check for costs
     * @param wand A optional wand, to be used for mana costs
     * @param reducer An optional scale for costs
     * @return True if the mage has these costs.
     */
    boolean has(Mage mage, Wand wand, CostReducer reducer);

    /**
     * Whether or not a mage has the required costs.
     *
     * @param mage The mage to check for costs
     * @param wand A optional wand, to be used for mana costs
     * @return True if the mage has these costs.
     */
    boolean has(Mage mage, Wand wand);

    /**
     * Whether or not a mage has the required costs.
     *
     * @param mage The mage to check for costs
     * @return True if the mage has these costs.
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
     */
    void deduct(Mage mage, Wand wand);

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
     */
    void deduct(Mage mage);

    /**
     * Give these costs to the specified Mage
     *
     * @param mage The mage to give these costs to
     * @param caster A optional caster, to be used for mana costs
     * @return false if the costs could not be given (e.g. mage is at max already)
     */
    boolean give(Mage mage, CasterProperties caster);

    /**
     * Get the amount of this cost type a Mage has.
     *
     * @param mage The mage to check
     * @param caster A optional caster, to be used for mana costs
     * @return the amount of this cost type the Mage has.
     */
    double getBalance(Mage mage, CasterProperties caster);

    /**
     * Get a human-readable description of this cost.
     *
     * <p>For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * <p>This does not include the amount, and only the label - e.g. "Bread" or "Mana" or "XP".
     *
     * @param messages The Messages class for looking up localizations
     * @param reducer An optional scale for costs
     * @return A printable String to display this casting cost to a Player.
     */
    String getDescription(Messages messages, CostReducer reducer);

    /**
     * Get a human-readable description of this cost.
     *
     * <p>For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * <p>This does not include the amount, and only the label - e.g. "Bread" or "Mana" or "XP".
     *
     * @param messages The Messages class for looking up localizations
     * @return A printable String to display this casting cost to a Player.
     */
    String getDescription(Messages messages);

    /**
     * Get a human-readable description of this cost.
     *
     * <p>For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * <p>This includes the amount as well as the label - e.g. "2 Bread" or "30 Mana" or "50 XP".
     *
     * @param messages The Messages class for looking up localizations
     * @param reducer An optional scale for costs
     * @return A printable String to display this casting cost to a Player.
     */
    String getFullDescription(Messages messages, CostReducer reducer);

    /**
     * Get a human-readable description of this cost.
     *
     * <p>For XP, display text will be determined by the
     * CostReducer if it uses Mana.
     *
     * <p>This includes the amount as well as the label - e.g. "2 Bread" or "30 Mana" or "50 XP".
     *
     * @param messages The Messages class for looking up localizations
     * @return A printable String to display this casting cost to a Player.
     */
    String getFullDescription(Messages messages);

    /**
     * This will return the currency type key or item key.
     * @return
     */
    @Nonnull
    String getType();

    /**
     * Get a copy of the item represented by this cost.
     *
     * <p>This will return null for non-item costs.
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

    /**
     * Convert this to cost to a different type if the current cost is not supported.
     * If the first fallback is also not supported, the second fallback will be used.
     *
     * @param controller The controller to use to check for currency support
     * @param fallbackTypes The types to use if this costs' type is not supported
     * @return true if this cost was converted
     */
    boolean checkSupported(@Nonnull MageController controller, @Nonnull String...fallbackTypes);

    /**
     * Change the type of this cost, converting the amount using global conversion configurations.
     *
     * @param controller The controller to look up conversion rates
     * @param newType The new type of this cost.
     */
    void convert(@Nonnull MageController controller, @Nonnull String newType);
}
