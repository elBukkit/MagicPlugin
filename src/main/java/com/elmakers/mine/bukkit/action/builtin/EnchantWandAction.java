package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EnchantWandAction extends BaseSpellAction
{
    private int levels;

    public void prepare(CastContext context, ConfigurationSection parameters) {
        levels = parameters.getInt("levels", 30);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (levels <= 0) {
            return SpellResult.FAIL;
        }
        if (wand == null) {
            context.sendMessage(context.getMessage("no_wand"));
            return SpellResult.FAIL;
        }
        if (wand.enchant(levels, mage) == 0) {
            return SpellResult.FAIL;
        }
        return SpellResult.CAST;
	}
}
