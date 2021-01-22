package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class FillWandAction extends BaseSpellAction
{
    private int maxLevel;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        maxLevel = parameters.getInt("max_level", 0);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = context.getWand();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null) {
            context.showMessage("no_wand", "You must be holding a wand!");
            return SpellResult.FAIL;
        }
        wand.fill(player, maxLevel);
        return SpellResult.CAST;
    }
}
