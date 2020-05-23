package com.elmakers.mine.bukkit.api.magic;

public enum TriggerType {
    // MOB-ONLY
    SPAWN,

    // PLAYER-ONLY
    JOIN,

    // MAGE CLASS
    UNLOCK, LOCK,

    // COMMON
    INTERVAL, DEATH, DAMAGE, LAUNCH, DEAL_DAMAGE, KILL
}
