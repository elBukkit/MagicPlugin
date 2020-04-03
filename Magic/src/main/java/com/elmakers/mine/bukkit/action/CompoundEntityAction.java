package com.elmakers.mine.bukkit.action;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public abstract class CompoundEntityAction extends CompoundAction
{
    private List<WeakReference<Entity>> entities = new ArrayList<>();
    private int currentEntity = 0;

    public abstract void addEntities(CastContext context, List<WeakReference<Entity>> entities);

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        currentEntity = 0;
    }

    @Override
    public SpellResult start(CastContext context) {
        entities.clear();
        addEntities(context, entities);
        context.addWork(20 + entities.size());
        return SpellResult.NO_TARGET;
    }

    @Override
    public boolean next(CastContext context) {
        currentEntity++;
        return currentEntity < entities.size();
    }

    @Override
    public SpellResult step(CastContext context)
    {
        while (currentEntity < entities.size())
        {
            Entity entity = entities.get(currentEntity).get();
            if (entity == null)
            {
                currentEntity++;
                skippedActions(context);
                continue;
            }
            actionContext.setTargetEntity(entity);
            if (entity instanceof LivingEntity) {
                actionContext.setTargetLocation(((LivingEntity)entity).getEyeLocation());
            } else {
                actionContext.setTargetLocation(entity.getLocation());
            }
            return startActions();
        }

        return SpellResult.NO_ACTION;
    }

    @Nullable
    @Override
    public Object clone() {
        CompoundEntityAction action = (CompoundEntityAction)super.clone();
        if (action != null) {
            action.entities = new ArrayList<>(this.entities);
        }
        return action;
    }
}
