package com.elmakers.mine.bukkit.economy;

import java.text.DecimalFormat;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class BaseMagicCurrency implements Currency {
    public static DecimalFormat formatter = new DecimalFormat("#,###.00");
    public static DecimalFormat intFormatter = new DecimalFormat("#,###");

    protected final String key;
    protected final double worth;
    protected final double defaultValue;
    protected final Double maxValue;
    protected final Double minValue;
    protected final String name;
    protected final String singularName;
    protected final String shortName;
    protected final String amountTemplate;
    protected final MaterialAndData icon;

    protected BaseMagicCurrency(MageController controller, String key, ConfigurationSection configuration) {
        this.key = key;
        worth = configuration.getDouble("worth", 1);
        name = controller.getMessages().get("currency." + key + ".name");
        singularName = controller.getMessages().get("currency." + key + ".name_singular", name);
        shortName = controller.getMessages().get("currency." + key + ".name_short", singularName);
        amountTemplate = controller.getMessages().get("currency." + key + ".amount", "$amount " + shortName);
        defaultValue = configuration.getDouble("default", 0);
        icon = ConfigurationUtils.getMaterialAndData(configuration, "icon");
        if (configuration.contains("max")) {
            maxValue = configuration.getDouble("max");
        } else {
            maxValue = null;
        }
        if (configuration.contains("min")) {
            minValue = configuration.getDouble("min");
        } else {
            minValue = null;
        }
    }

    protected BaseMagicCurrency(String key, double worth) {
        this.key = key;
        this.worth = worth;
        this.defaultValue = 0;
        maxValue = null;
        minValue = null;
        name = null;
        singularName = null;
        shortName = null;
        amountTemplate = null;
        icon = null;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public double getDefaultValue() {
        return defaultValue;
    }

    @Override
    public double getMaxValue() {
        return maxValue == null ? 0 : maxValue;
    }

    @Override
    public boolean hasMaxValue() {
        return maxValue != null;
    }

    @Override
    public double getMinValue() {
        return minValue == null ? 0 : minValue;
    }

    @Override
    public boolean hasMinValue() {
        return minValue != null;
    }

    @Override
    public double getWorth() {
        return worth;
    }

    @Override
    @Nullable
    public MaterialAndData getIcon() {
        return icon;
    }

    @Override
    public String getName(Messages messages) {
        return name;
    }

    @Override
    public String getShortName(Messages messages) {
        return shortName;
    }

    @Override
    public String getSingularName(Messages messages) {
        return singularName;
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
