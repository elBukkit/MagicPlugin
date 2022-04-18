package com.elmakers.mine.bukkit.action;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public abstract class CompoundEntityAction extends CompoundAction
{
    private List<WeakReference<Entity>> entities = new ArrayList<>();
    private int currentEntity = 0;
    private boolean sort = false;

    public abstract void addEntities(CastContext context, List<WeakReference<Entity>> entities);

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        sort = parameters.getBoolean("sort", false);
    }

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
        if (sort) {
            final Location target = context.getTargetLocation();
            final Location source = target == null ? context.getLocation() : target;
            Collections.sort(entities, new Comparator<WeakReference<Entity>>() {
                @Override
                public int compare(WeakReference<Entity> r1, WeakReference<Entity> r2) {
                    Entity e1 = r1.get();
                    Entity e2 = r2.get();
                    if (e1 == null && e2 == null) return 0;
                    if (e1 == null && e2 != null) return 1;
                    if (e1 != null && e2 == null) return -1;
                    return (int)(e1.getLocation().distanceSquared(source) - e2.getLocation().distanceSquared(source));
                }
            });
        }
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
