package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

public class ModifyManaAction extends BaseSpellAction
{
    private int mana;
    private boolean fillMana;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        mana = parameters.getInt("mana", 1);
        fillMana = parameters.getBoolean("fill_mana", false);
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
        double currentMana = wand.getMana();
        if (mana < 0 && currentMana <= 0) {
            return SpellResult.NO_TARGET;
        }
        int manaMax = wand.getManaMax();
        if (mana > 0 && currentMana >= manaMax) {
            return SpellResult.NO_TARGET;
        }
        if (fillMana) {
            currentMana = manaMax;
        } else {
            currentMana = Math.min(Math.max(0, currentMana + mana), manaMax);
        }
        wand.setMana((float)currentMana);
        wand.updateMana();
        return SpellResult.CAST;
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("mana");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("mana")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_INTEGERS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
