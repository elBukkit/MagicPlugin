package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.item.Cost;

public class CastingCost extends Cost implements com.elmakers.mine.bukkit.api.spell.CastingCost
{
    public CastingCost(MageController controller, String key, double cost)
    {
        super(controller, key, cost);
    }

    @Override
    public boolean has(Spell spell)
    {
        CastContext context = spell.getCurrentCast();
        Mage mage = context.getMage();
        Wand wand = context.getWand();
        return has(mage, wand, spell);
    }

    @Override
    public void use(Spell spell)
    {
        CastContext context = spell.getCurrentCast();
        Mage mage = context.getMage();
        Wand wand = context.getWand();
        deduct(mage, wand, spell);
    }

    @Override
    @Deprecated
    public int getXP() {
        return getXP(null);
    }

    @Override
    public int getMana() {
        return getMana(null);
    }
}
