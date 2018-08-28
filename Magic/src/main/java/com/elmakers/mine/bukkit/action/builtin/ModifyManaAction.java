package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;

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
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        MageController controller = context.getController();
        Mage mage = controller.getRegisteredMage(target);
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Wand wand = mage.getActiveWand();
        MageClass activeClass = mage.getActiveClass();
        CasterProperties caster = (wand != null && wand.getManaMax() > 0) ? wand : activeClass;
        if (caster == null) {
            caster = mage.getProperties();
        }
        if (caster.getEffectiveManaMax() <= 0) {
            return SpellResult.NO_TARGET;
        }
        double currentMana = caster.getMana();
        if (mana < 0 && currentMana <= 0) {
            return SpellResult.NO_TARGET;
        }
        int manaMax = caster.getEffectiveManaMax();
        if (mana > 0 && currentMana >= manaMax) {
            return SpellResult.NO_TARGET;
        }
        if (fillMana) {
            currentMana = manaMax;
        } else {
            currentMana = Math.min(Math.max(0, currentMana + mana), manaMax);
        }
        caster.setMana((float)currentMana);
        mage.updateMana();
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
