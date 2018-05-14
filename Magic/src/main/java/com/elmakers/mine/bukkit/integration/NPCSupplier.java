package com.elmakers.mine.bukkit.integration;

import org.bukkit.entity.Entity;

/**
 * Supplies information about NPCs.
 */
public interface NPCSupplier {
    /**
     * Determines whether or not an entity is a NPC.
     *
     * @param entity
     *            The entity to check.
     * @return True if this entity is a NPC.
     */
    boolean isNPC(Entity entity);

    /**
     * Determines whether or not an entity is a static NPC.
     *
     * <p>Static NPCs are always ignored by spells.
     *
     * @param entity
     *            The entity to check.
     * @return True if this entity is a static NPC.
     */
    boolean isStaticNPC(Entity entity);
}
