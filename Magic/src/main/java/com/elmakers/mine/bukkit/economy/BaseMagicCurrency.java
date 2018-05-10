package com.elmakers.mine.bukkit.economy;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;

public abstract class BaseMagicCurrency implements Currency {
    protected final String key;
    protected final double worth;
    protected final String name;
    protected final String amountTemplate;
    protected static final DecimalFormat formatter = new DecimalFormat("#,###.00");
    protected static final DecimalFormat intFormatter = new DecimalFormat("#,###");

    protected BaseMagicCurrency(MageController controller, String key, double worth) {
        this(key, worth, controller.getMessages().get("costs." + key),
                controller.getMessages().get("costs." + key + "_amount"));
    }

    protected BaseMagicCurrency(String key, double worth, String name, String amountTemplate) {
        this.key = key;
        this.worth = worth;
        this.name = name;
        this.amountTemplate = amountTemplate;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public double getDefaultValue() {
        return 0;
    }

    @Override
    public double getMaxValue() {
        return 0;
    }

    @Override
    public boolean hasMaxValue() {
        return false;
    }

    @Override
    public double getWorth() {
        return worth;
    }

    @Override
    @Nullable
    public MaterialAndData getIcon() {
        return null;
    }

    @Override
    public String getName(Messages messages) {
        return name;
    }

    protected boolean hasDecimals() {
        return false;
    }

    protected int getRoundedAmount(double amount) {
        return (int)Math.ceil(amount);
    }

    @Override
    public String formatAmount(double amount, Messages messages) {
        String amountString = hasDecimals()
                ? formatter.format(amount)
                : intFormatter.format(getRoundedAmount(amount));
        return amountTemplate.replace("$amount", amountString);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
