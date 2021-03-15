package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class HealthCurrency extends BaseMagicCurrency {
    public HealthCurrency(MageController controller, ConfigurationSection configuration) {
        super(controller, "health", configuration);
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        double balance = 0;
        LivingEntity living = mage.getLivingEntity();
        if (living != null) {
            balance = living.getHealth();
        }
        return balance;
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        LivingEntity living = mage.getLivingEntity();
        return living != null && living.getHealth() >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        LivingEntity living = mage.getLivingEntity();
        if (living != null) {
            living.setHealth(Math.max(0, living.getHealth() - amount));
        }
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        LivingEntity living = mage.getLivingEntity();
        if (living != null) {
            double maxHealth = CompatibilityUtils.getMaxHealth(living);
            if (living.getHealth() >= maxHealth) {
                return false;
            }
            living.setHealth(Math.min(maxHealth, living.getHealth() + amount));
            return true;
        }
        return false;
    }
}
