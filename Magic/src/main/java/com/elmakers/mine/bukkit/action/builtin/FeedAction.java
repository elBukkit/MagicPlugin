package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class FeedAction extends BaseSpellAction
{
    private static int MAX_FOOD_LEVEL = 20;

    private Integer foodLevel;
    private int feedAmount;
    private float saturationAmount;
    private boolean clearExhaustion;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        int defaultFeed = 20;
        float defaultSaturation = 20;
        if (parameters.contains("food_level")) {
            defaultFeed = 0;
            defaultSaturation = 0;
            foodLevel = parameters.getInt("food_level");
        } else {
            foodLevel = null;
        }
        feedAmount = parameters.getInt("feed", defaultFeed);
        saturationAmount = (float)parameters.getDouble("saturation", defaultSaturation);
        clearExhaustion = parameters.getBoolean("exhaustion", true);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity targetEntity = context.getTargetEntity();
        if (!(targetEntity instanceof Player))
        {
            return SpellResult.NO_TARGET;
        }

        Player player = (Player)targetEntity;
        if (feedAmount > 0 && player.getFoodLevel() >= MAX_FOOD_LEVEL)
        {
            return SpellResult.NO_TARGET;
        }
        if (feedAmount < 0 && player.getFoodLevel() == 0)
        {
            return SpellResult.NO_TARGET;
        }
        if (clearExhaustion)
        {
            player.setExhaustion(0);
        }
        if (saturationAmount != 0)
        {
            player.setSaturation(saturationAmount);
        }
        if (foodLevel != null) {
            player.setFoodLevel(foodLevel);
        }
        if (feedAmount != 0)
        {
            player.setFoodLevel(Math.min(MAX_FOOD_LEVEL, Math.max(0, player.getFoodLevel() + feedAmount)));
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("food_level");
        parameters.add("feed");
        parameters.add("saturation");
        parameters.add("exhaustion");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("food_level") || parameterKey.equals("feed") ||  parameterKey.equals("saturation")) {
            examples.add("0");
            examples.add("1");
            examples.add("10");
        } else if (parameterKey.equals("exhaustion")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
