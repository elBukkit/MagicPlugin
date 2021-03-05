package com.elmakers.mine.bukkit.api.effect;

public interface EffectPlay {
    void cancel();
    default boolean isPlayer(EffectPlayer player) { return false; }
}
