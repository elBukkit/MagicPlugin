package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class ManaCurrency extends BaseMagicCurrency {
    public ManaCurrency(MageController controller, ConfigurationSection configuration) {
        super(controller, "mana", configuration);
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        if (caster == null) {
            caster = mage.getActiveProperties();
        }
        return caster.getMana();
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        if (caster == null) {
            caster = mage.getActiveProperties();
        }
        return caster.getMana() >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        if (caster == null) {
            caster = mage.getActiveProperties();
        }
        caster.removeMana((float)amount);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        if (caster == null) {
            caster = mage.getActiveProperties();
        }
        if (caster.getMana() >= caster.getManaMax()) {
            return false;
        }
        float newMana = (float)Math.min(caster.getManaMax(), caster.getMana() + amount);
        caster.setMana(newMana);
        return true;
    }
}
