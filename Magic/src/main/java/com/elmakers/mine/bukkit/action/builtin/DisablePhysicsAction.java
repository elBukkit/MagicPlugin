package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class DisablePhysicsAction extends BaseSpellAction
{
    private int duration;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        duration = parameters.getInt("duration", 1);
        duration += parameters.getInt("undo", 0);
        duration += parameters.getInt("physics_buffer", 1000);
    }

    @Override
    public SpellResult perform(CastContext context) {
        MageController controller = context.getController();
        controller.disablePhysics(duration);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        }
    }
}
