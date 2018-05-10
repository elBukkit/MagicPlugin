package com.elmakers.mine.bukkit.economy;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class LevelCurrency extends BaseMagicCurrency {
    public LevelCurrency(MageController controller) {
        super(controller, "levels", 1);
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        return mage.getLevel();
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return mage.getLevel() >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        int newLevel = Math.max(0, mage.getLevel() - getRoundedAmount(amount));
        mage.setLevel(newLevel);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        int newLevel = mage.getLevel() + getRoundedAmount(amount);
        mage.setLevel(newLevel);
        return mage.isPlayer();
    }
}
