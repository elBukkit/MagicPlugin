package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class CustomCurrency extends BaseMagicCurrency {

    public CustomCurrency(MageController controller, String key, ConfigurationSection configuration) {
        super(controller, key, configuration);
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
}
