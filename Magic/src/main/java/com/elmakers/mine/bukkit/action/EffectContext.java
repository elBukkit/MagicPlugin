package com.elmakers.mine.bukkit.action;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.action.CastContext;

public class EffectContext {
    private final CastContext context;
    private final String effectKey;

    public EffectContext(String effectKey, CastContext context, Entity source) {
        this.effectKey = effectKey;
        this.context = new com.elmakers.mine.bukkit.action.CastContext(context, source);
    }

    public EffectContext(String effectKey, CastContext context) {
        this.effectKey = effectKey;
        this.context = context;
    }

    public void perform() {
        context.playEffects(effectKey);
    }

    public CastContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "FX: " + effectKey + " on " + context;
    }
}
