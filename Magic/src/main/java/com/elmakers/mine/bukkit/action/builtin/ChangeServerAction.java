package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ChangeServerAction extends BaseSpellAction
{
    private String serverName;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        serverName = parameters.getString("server", "");
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
        context.getController().sendPlayerToServer(player, serverName);

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
