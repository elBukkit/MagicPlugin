package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.Targeting;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseProjectileAction extends CompoundAction {
    private long lifetime;
    private boolean setTarget;

    protected boolean track = false;

    private Set<Entity> tracking;
    private long expiration;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        lifetime = parameters.getLong("lifetime", 10000);
        setTarget = parameters.getBoolean("set_target", false);
        track = parameters.getBoolean("track_projectile", track);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        expiration = System.currentTimeMillis() + lifetime;
        tracking = null;
    }

    @Override
    public SpellResult step(CastContext context)
    {
        if (tracking == null || tracking.size() == 0) {
            tracking = null;
            return SpellResult.CAST;
        }

        if (System.currentTimeMillis() > expiration) {
            for (Entity entity : tracking) {
                entity.removeMetadata("track", context.getPlugin());
                entity.removeMetadata("damaged", context.getPlugin());
                entity.removeMetadata("hit", context.getPlugin());
                entity.remove();
            }
            context.getMage().sendDebugMessage(ChatColor.DARK_GRAY + "Projectiles expired", 4);
            tracking = null;
            return SpellResult.NO_TARGET;
        }

        for (Entity entity : tracking)
        {
            if (!entity.isValid() || entity.hasMetadata("hit"))
            {
                tracking.remove(entity);
                Plugin plugin = context.getPlugin();
                entity.removeMetadata("track", plugin);
                Entity targetEntity = null;
                Location targetLocation = entity.getLocation();
                List<MetadataValue> metadata = entity.getMetadata("hit");
                for (MetadataValue value : metadata) {
                    if (value.getOwningPlugin().equals(plugin)) {
                        Object o = value.value();
                        if (o != null && o instanceof WeakReference) {
                            WeakReference<?> reference = (WeakReference<?>)o;
                            o = reference.get();
                            if (o != null && o instanceof Entity) {
                                targetEntity = (Entity)o;
                                targetLocation = targetEntity.getLocation();
                            }
                            break;
                        }
                    }
                }
                if (targetEntity == null) {
                    context.getMage().sendDebugMessage(ChatColor.GRAY + "Projectile missed", 4);
                } else {
                    context.getMage().sendDebugMessage(ChatColor.GREEN + "Projectile hit " + ChatColor.GOLD + targetEntity.getType());
                }
                entity.removeMetadata("hit", plugin);
                createActionContext(context, context.getMage().getEntity(), entity.getLocation(), targetEntity, targetLocation);
                actionContext.playEffects("hit");
                SpellResult result = startActions();
                if (targetEntity != null) {
                    result = result.min(SpellResult.CAST);
                } else {
                    result = result.min(SpellResult.NO_TARGET);
                }
                context.addResult(result);
                return result;
            }
        }

        return SpellResult.PENDING;
    }

    @Override
    public boolean next(CastContext context) {
        return tracking != null && tracking.size() > 0;
    }

    protected void track(CastContext context, Entity entity) {
        if (tracking == null) {
            tracking = new HashSet<>();
        }
        tracking.add(entity);
        context.registerForUndo(entity);
        if (setTarget) {
            context.setTargetEntity(entity);
        }
        Collection<EffectPlayer> projectileEffects = context.getEffects("projectile");
        for (EffectPlayer effectPlayer : projectileEffects) {
            effectPlayer.start(entity.getLocation(), entity, null, null);
        }
        if (track) {
            Targeting.track(context.getPlugin(), entity);
        }
    }

    protected SpellResult checkTracking(CastContext context) {
        if (tracking == null) {
            return SpellResult.FAIL;
        }
        if (!track && !hasActions()) {
            // Don't bother tracking if we're not doing anything on hit
            if (!context.hasEffects("hit")) {
                tracking = null;
            }
            return SpellResult.CAST;
        }

        return SpellResult.NO_TARGET;
    }
}
