package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class TriggerAction extends BaseSpellAction
{
    private String trigger;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        trigger = parameters.getString("trigger");
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (trigger == null) {
            return SpellResult.FAIL;
        }
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        Mage mage = context.getController().getRegisteredMage(target);
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }

        if (!mage.trigger(trigger)) {
            return SpellResult.NO_TARGET;
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("trigger");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("trigger")) {
            examples.add("interval");
            examples.add("destruct");
            examples.add("damage");
            examples.add("death");
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
