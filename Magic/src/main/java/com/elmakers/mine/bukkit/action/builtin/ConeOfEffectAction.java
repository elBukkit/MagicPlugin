package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.utility.Targeting;

public class ConeOfEffectAction extends CompoundEntityAction
{
    private int targetCount;
    private double range;
    private Targeting targeting;

    @Override
    public void initialize(Spell spell, ConfigurationSection baseParameters) {
        super.initialize(spell, baseParameters);
        targeting = new Targeting();
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        targeting.start(context.getEyeLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        targetCount = parameters.getInt("target_count", -1);
        range = parameters.getDouble("range", 16);
        range = range * context.getMage().getRangeMultiplier();

        targeting.processParameters(parameters);

        // Some parameter tweaks to make sure things are sane
        TargetType targetType = targeting.getTargetType();
        if (targetType == TargetType.NONE || targetType == TargetType.BLOCK) {
            targeting.setTargetType(TargetType.OTHER);
        } else if (targetType == TargetType.SELF) {
            targeting.setTargetType(TargetType.ANY);
        }

        // COE never uses hitbox, there's the Retarget action for that.
        targeting.setUseHitbox(false);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities) {
        context.addWork((int)Math.ceil(range) + 100);
        targeting.getTargetEntities(context, range, targetCount, entities);
    }
}
