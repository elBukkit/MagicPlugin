package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;

public class InventoryCloseAction extends BaseSpellAction {

    @Override
    public SpellResult perform(CastContext castContext) {
        Entity targetEntity = castContext.getTargetEntity();
        if (!(targetEntity instanceof HumanEntity)) {
            return SpellResult.NO_TARGET;
        }
        ((HumanEntity) targetEntity).closeInventory();
        return SpellResult.CAST;
    }
}

