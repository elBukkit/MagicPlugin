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
        Entity sourceEntity = context.getLivingEntity();
        if (!(target instanceof Creature)) {
            boolean result = false;
            if (setPathfinder) {
                result = CompatibilityLib.getMobUtils().setPathfinderTarget(target, sourceEntity, speedModifier);
            }
            return result ? SpellResult.CAST : SpellResult.NO_TARGET;
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
        if (sourceEntity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(sourceEntity instanceof LivingEntity)) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

        LivingEntity source = (LivingEntity)sourceEntity;
        LivingEntity current = creatureTarget.getTarget();
        if (source == current) {
            return SpellResult.NO_ACTION;
        }

        ((Creature) target).setTarget(source);
        if (setPathfinder) {
            CompatibilityLib.getMobUtils().setPathfinderTarget(target, source, speedModifier);
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
