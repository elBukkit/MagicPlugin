package com.elmakers.mine.bukkit.api.magic;

public enum TriggerType {
    // MOB
    SPAWN,

    // PLAYER
    JOIN,

    // MODIFIER
    ADDED, REMOVED,

    // MAGE CLASS
    UNLOCK, LOCK,

    // COMMON
    INTERVAL, DEATH, DAMAGE, LAUNCH, DEAL_DAMAGE, KILL, SNEAK, STOP_SNEAK,
    SPRINT, STOP_SPRINT, GLIDE, STOP_GLIDE,
    RIGHT_CLICK, LEFT_CLICK
}
