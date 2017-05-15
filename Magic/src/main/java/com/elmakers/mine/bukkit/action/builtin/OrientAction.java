package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class OrientAction extends BaseSpellAction {
    private Float pitch;
    private Float yaw;
    private Float pitchOffset;
    private Float yawOffset;

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
        }if (parameters.contains("pitch_offset")) {
            pitchOffset = (float)parameters.getDouble("pitch_offset");
        } else {
            pitchOffset = null;
        }
        if (parameters.contains("yaw_offset")) {
            yawOffset = (float)parameters.getDouble("yaw_offset");
        } else {
            yawOffset = null;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Entity entity = mage.getEntity();
        if (entity == null)
        {
            return SpellResult.ENTITY_REQUIRED;
        }

        context.registerMoved(entity);
        Location location = entity.getLocation();
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
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null)
            {
                return SpellResult.NO_TARGET;
            }
            Location direction = targetEntity.getLocation().subtract(location);
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
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
