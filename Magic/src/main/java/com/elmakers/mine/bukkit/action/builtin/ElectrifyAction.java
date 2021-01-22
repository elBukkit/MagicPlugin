package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ElectrifyAction extends BaseSpellAction
{
    private boolean electrify;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        electrify = parameters.getBoolean("electrify", true);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();

        if (entity == null || !(entity instanceof Creeper)) {
            return SpellResult.NO_TARGET;
        }
        Creeper creeper = (Creeper)entity;
        if (creeper.isPowered() == electrify) {
            return SpellResult.NO_ACTION;
        }
        creeper.setPowered(electrify);
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
