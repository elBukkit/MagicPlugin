package com.elmakers.mine.bukkit.api.entity;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;

/**
 * Register via PreLoadEvent.register()
 */
public interface TeamProvider extends MagicProvider {
    /**
     * Check to see if one entity is friendly (on a team with) another entity.
     *
     * <p>This will be called any time on entity (the attacker) is targeting another
     * entity.
     *
     * <p>Friendly entities generally can't target each other, unless a spell has
     * bypass_friendly_fire (generally true for healing spells).
     *
     * <p>Spells may also be marked only_friendly, meaning it can only be cast on friendly targets.
     *
     * <p>The default state for two entities is that they are not friendly with each other.
     *
     * @param attacker The entity targeting another entity
     * @param entity The entity being targeted
     * @return true if the attacker is friends with the target
     */
    boolean isFriendly(Entity attacker, Entity entity);
}
