package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;

public class EffectContext {
    private final CastContext context;
    private final String effectKey;

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
}
