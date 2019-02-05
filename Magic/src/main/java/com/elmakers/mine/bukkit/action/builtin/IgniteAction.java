package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class IgniteAction extends BaseSpellAction
{
    private int duration;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        duration = parameters.getInt("duration", 5000);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        int ticks = duration * 20 / 1000;
        Entity entity = context.getTargetEntity();
        MageController controller = context.getController();
        boolean isElemental = controller.isElemental(entity);
        if (!isElemental && entity.getFireTicks() == ticks)
        {
            return SpellResult.NO_TARGET;
        }
        context.registerDamaged(entity);

        if (isElemental) {
            Mage mage = context.getMage();
            controller.damageElemental(entity, 0, ticks, mage.getCommandSender());
        } else {
            entity.setFireTicks(ticks);
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
