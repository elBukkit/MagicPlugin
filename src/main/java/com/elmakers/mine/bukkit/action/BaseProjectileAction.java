package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Collection;

public abstract class BaseProjectileAction extends CompoundAction {
    private long lifetime;

    protected Entity tracking;
    private long expiration;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        lifetime = parameters.getLong("lifetime", 10000);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        expiration = System.currentTimeMillis() + lifetime;
    }

    @Override
    public SpellResult step(CastContext context)
    {
        if (tracking == null) {
            return SpellResult.CAST;
        }

        if (System.currentTimeMillis() > expiration) {
            tracking.remove();
            tracking = null;
            return SpellResult.NO_TARGET;
        }

        if (!tracking.isValid()) {
            createActionContext(context, tracking, tracking.getLocation(), null, tracking.getLocation());
            actionContext.playEffects("hit");
            tracking = null;
            return startActions();
        }

        return SpellResult.PENDING;
    }

    protected void playProjectileEffects(CastContext context) {
        Collection<EffectPlayer> projectileEffects = context.getEffects("projectile");
        for (EffectPlayer effectPlayer : projectileEffects) {
            effectPlayer.start(tracking.getLocation(), tracking, null, null);
        }
    }

    protected SpellResult checkTracking(CastContext context) {
        if (tracking == null) {
            return SpellResult.FAIL;
        }
        if (!hasActions()) {
            // Don't bother tracking if we're not doing anything on hit
            if (!context.hasEffects("hit")) {
                tracking = null;
            }
            return SpellResult.CAST;
        }

        return SpellResult.NO_ACTION;
    }
}
