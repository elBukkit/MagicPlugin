package com.elmakers.mine.bukkit.economy;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class HungerCurrency extends BaseMagicCurrency {
    public HungerCurrency(MageController controller) {
        super(controller, "hunger", 1);
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        Player player = mage.getPlayer();
        return player == null ? 0 : player.getFoodLevel();
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        Player player = mage.getPlayer();
        return player != null && player.getFoodLevel() >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        Player player = mage.getPlayer();
        if (player != null) {
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - getRoundedAmount(amount)));
        }
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        Player player = mage.getPlayer();
        if (player != null) {
            if (player.getFoodLevel() >= 10) {
                return false;
            }
            player.setFoodLevel(Math.min(10, player.getFoodLevel() + getRoundedAmount(amount)));
            return true;
        }
        return false;
    }
}
