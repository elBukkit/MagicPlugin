package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class WeatherAction extends BaseSpellAction
{
    private String weatherString = "";
    private boolean playerWeather = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        weatherString = parameters.getString("weather", "");
        playerWeather = parameters.getBoolean("player_weather", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        World world = context.getWorld();
        if (world == null) {
            return SpellResult.WORLD_REQUIRED;
        }
        Entity targetEntity = context.getTargetEntity();
        Player targetPlayer = targetEntity instanceof Player ? (Player)targetEntity : null;
        if (playerWeather && targetPlayer == null) {
            return SpellResult.NO_TARGET;
        }
        boolean hasStorm = world.hasStorm();
        boolean makeStorm = weatherString.equals("storm");
        if (weatherString.equals("cycle")) {
            makeStorm = !hasStorm;
        }

        if (playerWeather && weatherString.equals("world")) {
            targetPlayer.setPlayerWeather(world.hasStorm() || world.isThundering() ? WeatherType.DOWNFALL : WeatherType.CLEAR);
        } else if (weatherString.equals("rain")) {
            if (playerWeather) {
                targetPlayer.setPlayerWeather(WeatherType.DOWNFALL);
            } else {
                world.setStorm(true);
            }
        } else if (makeStorm) {
            if (playerWeather) {
                targetPlayer.setPlayerWeather(WeatherType.DOWNFALL);
            } else {
                world.setStorm(true);
                world.setThundering(true);
            }
        } else {
            if (playerWeather) {
                targetPlayer.setPlayerWeather(WeatherType.CLEAR);
            } else {
                world.setStorm(false);
                world.setThundering(false);
            }
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
