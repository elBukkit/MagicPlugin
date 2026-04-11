package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.SkillTreeMenu;

public class OpenValhallaSkillTreeAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null || !(targetEntity instanceof Player)) {
            return SpellResult.NO_TARGET;
        }
        Entity sourceEntity = context.getEntity();
        if (sourceEntity == null || !(sourceEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        new SkillTreeMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player)sourceEntity), (Player)targetEntity).open();

        return SpellResult.CAST;
    }
}
