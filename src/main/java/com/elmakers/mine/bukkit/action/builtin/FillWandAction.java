package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class FillWandAction extends BaseSpellAction
{
    private int maxLevel;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        maxLevel = parameters.getInt("max_level", 0);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null) {
            context.sendMessage("no_wand");
            return SpellResult.FAIL;
        }
        wand.fill(player, maxLevel);
        return SpellResult.CAST;
    }
}
