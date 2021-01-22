package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class WeatherAction extends BaseSpellAction
{
    private String weatherString = "";

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        weatherString = parameters.getString("weather", "");
    }

    @Override
    public SpellResult perform(CastContext context) {
        World world = context.getWorld();
        if (world == null) {
            return SpellResult.WORLD_REQUIRED;
        }
        boolean hasStorm = world.hasStorm();
        boolean makeStorm = weatherString.equals("storm");
        if (weatherString.equals("cycle")) {
            makeStorm = !hasStorm;
        }

        if (weatherString.equals("rain")) {
            world.setStorm(true);
        } else if (makeStorm) {
            world.setStorm(true);
            world.setThundering(true);
        } else {
            world.setStorm(false);
            world.setThundering(false);
        }
        return makeStorm ? SpellResult.CAST : SpellResult.ALTERNATE;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("weather");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("weather")) {
            examples.add("rain");
            examples.add("storm");
            examples.add("cycle");
            examples.add("clear");
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
