package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class InventoryCloseAction extends BaseSpellAction {

    @Override
    public SpellResult perform(CastContext castContext) {
        castContext.getMage().getPlayer().closeInventory();
        return SpellResult.CAST;
    }
}

