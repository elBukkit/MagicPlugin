package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class OrientAction extends BaseSpellAction {
    private Float pitch;
    private Float yaw;
    private Float pitchOffset;
    private Float yawOffset;
    private boolean orientTarget;
    private boolean targetBlock;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        if (parameters.contains("pitch")) {
            pitch = (float)parameters.getDouble("pitch");
        } else {
            pitch = null;
        }
        if (parameters.contains("yaw")) {
            yaw = (float)parameters.getDouble("yaw");
        } else {
            yaw = null;
        }
        if (parameters.contains("pitch_offset")) {
            pitchOffset = (float)parameters.getDouble("pitch_offset");
        } else {
            pitchOffset = null;
        }
        if (parameters.contains("yaw_offset")) {
            yawOffset = (float)parameters.getDouble("yaw_offset");
        } else {
            yawOffset = null;
        }
        orientTarget = parameters.getBoolean("orient_target", false);
        targetBlock = parameters.getBoolean("target_block", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Entity entity = orientTarget ? context.getTargetEntity() : mage.getEntity();
        if (entity == null)
        {
            return orientTarget ? SpellResult.NO_TARGET : SpellResult.ENTITY_REQUIRED;
        }

        Location location = entity.getLocation();
        context.registerMoved(entity);
        if (pitch != null || yaw != null)
        {
            if (pitch != null) {
                location.setPitch(pitch);
            }
            if (yaw != null) {
                location.setYaw(yaw);
            }
        }
        if (pitchOffset != null || yawOffset != null)
        {
            if (pitchOffset != null) {
                location.setPitch(location.getPitch() + pitchOffset);
            }
            if (yawOffset != null) {
                location.setYaw(location.getYaw() + yawOffset);
            }
        }
        if (pitchOffset == null && yawOffset == null && yaw == null && pitch == null)
        {
            Entity targetEntity = orientTarget ? mage.getEntity() : context.getTargetEntity();
            if (targetEntity == null && orientTarget) {
                return SpellResult.ENTITY_REQUIRED;
            }
            Location targetLocation = null;
            if (targetBlock) {
                targetLocation = context.getTargetLocation();
            } else {
                targetLocation = targetEntity == null ? null : targetEntity.getLocation();
            }
            if (targetLocation == null) {
                return SpellResult.NO_TARGET;
            }
            Location direction = targetLocation.clone().subtract(location);
            location.setDirection(direction.toVector());
        }
        entity.teleport(location);

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("pitch");
        parameters.add("yaw");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("pitch") || parameterKey.equals("yaw")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
