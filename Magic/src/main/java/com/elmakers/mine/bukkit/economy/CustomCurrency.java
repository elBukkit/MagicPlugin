package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CustomCurrency extends BaseMagicCurrency {
    private final double defaultValue;
    private final double maxValue;
    private final MaterialAndData icon;

    public CustomCurrency(MageController controller, String key, ConfigurationSection configuration) {
        super(key, configuration.getDouble("worth", 1),
                controller.getMessages().get("currency." + key + ".name", key),
                controller.getMessages().get("currency." + key + ".amount", key));
        defaultValue = configuration.getDouble("default", 0);
        maxValue = configuration.getDouble("max", -1);
        icon = ConfigurationUtils.getMaterialAndData(configuration, "icon");
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        return mage.getCurrency(key);
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return mage.getCurrency(key) >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        mage.removeCurrency(key, amount);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        if (mage.isAtMaxCurrency(key)) {
            return false;
        }
        mage.addCurrency(key, amount);
        return true;
    }

    @Override
    public double getDefaultValue() {
        return defaultValue;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean hasMaxValue() {
        return maxValue >= 0;
    }

    @Override
    public MaterialAndData getIcon() {
        return icon;
    }
}
