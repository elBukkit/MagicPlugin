package com.elmakers.mine.bukkit.api.economy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;

public interface Currency {
    /**
     * Each currency must have a unique key name, used to register.
     * This will be the key used in all configs and /mgive commands.
     *
     * @return the key name of this currency.
     */
    @Nonnull
    String getKey();

    /**
     * Get the amount of this currency type a Mage has.
     *
     * @param mage The mage to check
     * @param caster A optional caster, to be used for wand or class-specific costs (such as mana)
     * @return the amount of this cost type the Mage has.
     */
    double getBalance(Mage mage, CasterProperties caster);

    /**
     * Whether or not a mage has at least a certain amount of this currency.
     *
     * @param mage The mage to check for costs
     * @param caster A optional wand or mage class, to be used for mana or other specific currencies
     * @param amount The amount of currency required.
     * @return True if the mage has these costs.
     */
    boolean has(Mage mage, CasterProperties caster, double amount);

    /**
     * Deduct some currency from the specified Mage
     *
     * @param mage The mage to check for costs
     * @param caster A optional caster, to be used for mana costs
     * @param amount The amount of currency to deduct.
     */
    void deduct(Mage mage, CasterProperties caster, double amount);

    /**
     * Give some currency to the specified Mage
     *
     * @param mage The mage to give these costs to
     * @param caster A optional caster, to be used for mana costs
     * @param amount The amount of currency to give.
     * @return false if the costs could not be given (e.g. mage is at max already)
     */
    boolean give(Mage mage, CasterProperties caster, double amount);

    /**
     * Get the default value for this currency.
     * This will be used to initialize a player's balance.
     *
     * @return the default value of this currency.
     */
    double getDefaultValue();

    /**
     * Check to see if this currency has an upper limit for player balances.
     *
     * @return true if this currency should be limited
     */
    boolean hasMaxValue();

    /**
     * Get the maximum value for this currency.
     * Player balances will be capped to this value.
     * Only has an effect if @hasMaxValue returns true.
     *
     * @return The maximum limit for player balances of this currency
     */
    double getMaxValue();

    /**
     * Get an icon to use to represent this currency as a physical item.
     * This is used in commands such as /mgive sp:100
     *
     * @return
     */
    @Nullable
    MaterialAndData getIcon();

    /**
     * Get the relative worth of this currency. This is used for automatic currency conversions.
     * Most currencies weight their worth in terms of virtual currency (e.g. Vault), so if a currency returns 10
     * it is worth 10x standard virtual currency.
     *
     * @return
     */
    double getWorth();

    /**
     * Get a human-readable description of this currency.
     *
     * <p>This does not include the amount, and only the label - e.g. "Bread" or "Mana" or "XP".
     *
     * @param messages The Messages class for looking up localizations
     * @return A printable String to display this currency name to a Player.
     */
    @Nonnull
    String getName(Messages messages);

    /**
     * Get a human-readable description of an amount of this currency.
     *
     * <p>This includes the amount as well as the label - e.g. "2 Bread" or "30 Mana" or "50 XP".
     *
     * @param amount The amount
     * @param messages The Messages class for looking up localizations
     * @return A printable String to display this currency amount to a Player.
     */
    @Nonnull
    String formatAmount(double amount, Messages messages);

    /**
     * Check to see if this currency is valid.
     *
     * <p>This can be used if disabling currency, or providing a currency that may rely on some other dependency.
     * @return true if this currency is valid
     */
    boolean isValid();
}
