package com.elmakers.mine.bukkit.api.magic;

public enum TriggerType {
    // MOB-ONLY
    SPAWN,

    // PLAYER-ONLY
    JOIN,

    // COMMON
    INTERVAL, DEATH, DAMAGE, LAUNCH, DEAL_DAMAGE, TARGET_DEATH
}
