package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ClearFallProtectionAction extends BaseSpellAction {
    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        Mage targetMage = targetEntity != null
                ? context.getController().getRegisteredMage(targetEntity)
                : null;

        if (targetMage == null) {
            return SpellResult.NO_TARGET;
        }

        targetMage.clearFallProtection();
        return SpellResult.CAST;
    }
}
