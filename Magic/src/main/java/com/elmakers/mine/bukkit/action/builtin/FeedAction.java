package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class FeedAction extends BaseSpellAction
{
    private static int MAX_FOOD_LEVEL = 20;

    private int feedAmount;
    private float saturationAmount;
    private boolean clearExhaustion;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        feedAmount = parameters.getInt("feed", 20);
        saturationAmount = parameters.getInt("saturation", 20);
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
}
