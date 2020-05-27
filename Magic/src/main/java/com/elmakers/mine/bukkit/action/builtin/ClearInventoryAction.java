package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ClearInventoryAction extends BaseSpellAction {
    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity instanceof Player) {
            Player player = (Player)targetEntity;
            player.getInventory().clear();

            return SpellResult.CAST;
        }
        if (!(targetEntity instanceof LivingEntity)) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

        LivingEntity li = (LivingEntity)targetEntity;
        li.getEquipment().clear();
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
