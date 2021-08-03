package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class AggroAction extends BaseSpellAction
{
    private boolean clearTarget;
    private boolean setPathfinder;
    private double speedModifier;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        clearTarget = parameters.getBoolean("clear_target", false);
        setPathfinder = parameters.getBoolean("set_pathfinder", true);
        speedModifier = parameters.getDouble("speed_modifier", 1);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity target = context.getTargetEntity();
        if (!(target instanceof Creature))
        {
            return SpellResult.NO_TARGET;
        }

        Creature creatureTarget = (Creature)target;
        if (clearTarget) {
            LivingEntity current = creatureTarget.getTarget();
            if (current == null) {
                return SpellResult.NO_TARGET;
            }
            creatureTarget.setTarget(null);
            return SpellResult.CAST;
        }
        LivingEntity source = context.getLivingEntity();
        if (source == null)
        {
            return SpellResult.NO_TARGET;
        }

        LivingEntity current = creatureTarget.getTarget();
        if (source == current)
        {
            return SpellResult.NO_ACTION;
        }

        ((Creature) target).setTarget(source);
        if (setPathfinder) {
            CompatibilityLib.getCompatibilityUtils().setPathFinderTarget(target, source, speedModifier);
        }
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
