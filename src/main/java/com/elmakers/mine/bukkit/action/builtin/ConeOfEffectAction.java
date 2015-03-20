package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.List;

public class ConeOfEffectAction extends CompoundEntityAction
{
    private int targetCount;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        targetCount = parameters.getInt("target_count", -1);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities) {
        context.getTargetEntities(targetCount, entities);
    }
}
