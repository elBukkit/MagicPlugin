package com.elmakers.mine.bukkit.economy;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class SpellPointCurrency extends BaseMagicCurrency {
    private final boolean isValid;

    public SpellPointCurrency(MageController controller, double worth) {
        super(controller, "sp", worth);
        isValid = controller.isSPEnabled();
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        return mage.getSkillPoints();
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return mage.getSkillPoints() >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        mage.addSkillPoints(-getRoundedAmount(amount));
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        if (mage.isAtMaxSkillPoints()) {
            return false;
        }
        mage.addSkillPoints(getRoundedAmount(amount));
        return true;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }
}
