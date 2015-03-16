package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConeOfEffectAction extends CompoundEntityAction
{
    private int targetCount;
    private boolean useHitbox;
    private double range;
    private double fov;
    private double closeRange;
    private double closeFOV;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        targetCount = parameters.getInt("target_count", -1);
        useHitbox = parameters.getBoolean("hitbox", false);
        range = parameters.getInt("range", 32);
        fov = parameters.getDouble("fov", 0.3);
        closeRange = parameters.getDouble("close_range", 1);
        closeFOV = 0.5;

        if (parameters.contains("fov")) {
            closeFOV = fov;
        }
        closeFOV = parameters.getDouble("close_fov", closeFOV);
        super.prepare(context, parameters);
    }

    @Override
    public void prepareEntities(CastContext context, ConfigurationSection parameters, List<WeakReference<Entity>> entities) {
        Spell spell = context.getSpell();
        if (!(spell instanceof TargetingSpell)) {
            return;
        }

        List<Target> candidates = ((TargetingSpell) spell).getAllTargetEntities(context.getLocation(), context.getEntity(), range, fov, closeRange, closeFOV, useHitbox);
        if (targetCount < 0) {
            targetCount = entities.size();
        }

        for (int i = 0; i < targetCount && i < candidates.size(); i++) {
            Target target = candidates.get(i);
            entities.add(new WeakReference<Entity>(target.getEntity()));
        }
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("fov");
        parameters.add("target_count");
        parameters.add("hitbox");
        parameters.add("range");
        parameters.add("close_range");
        parameters.add("close_fov");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("hitbox")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("target_count") || parameterKey.equals("range") || parameterKey.equals("fov")
                || parameterKey.equals("close_range") || parameterKey.equals("close_fov")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
