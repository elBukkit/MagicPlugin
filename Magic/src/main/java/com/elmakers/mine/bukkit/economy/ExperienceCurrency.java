package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class ExperienceCurrency extends BaseMagicCurrency {
    public ExperienceCurrency(MageController controller, ConfigurationSection configuration) {
        super(controller, "xp", configuration);
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        return mage.getExperience();
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return mage.getExperience() >= getRoundedAmount(amount);
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        mage.removeExperience(getRoundedAmount(amount));
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        mage.giveExperience(getRoundedAmount(amount));
        return true;
    }
}
