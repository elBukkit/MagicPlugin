package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.MetaKey;

public class MagicMetaKeys {
    private MagicMetaKeys() {
    }

    /**
     * Entity meta key that tells magic to ignore it, both skipping it when
     * targeting and applying rollbacks.
     */
    public static final MetaKey<Boolean> NO_TARGET = new MetaKey<>(
            Boolean.class, "notarget");

    /**
     * Entity meta key that tells magic to avoid dropping items when the entity
     * dies.
     */
    public static final MetaKey<Boolean> NO_DROPS = new MetaKey<>(
            Boolean.class, "nodrops");

    public static final MetaKey<String> OWNER = new MetaKey<>(
            String.class, "owner");

    public static final MetaKey<String> MAGIC_MOB = new MetaKey<>(
            String.class, "magicmob");

    public static final MetaKey<String> NPC_ID = new MetaKey<>(
            String.class, "npc_id");

    public static final MetaKey<Boolean> BROOM = new MetaKey<>(
            Boolean.class, "broom");

    public static final MetaKey<Boolean> TEMPORARY = new MetaKey<>(
            Boolean.class, "temporary");

    public static final MetaKey<Long> AUTOMATION = new MetaKey<>(
            Long.class, "automation");

    public static final MetaKey<Boolean> CANCEL_EXPLOSION = new MetaKey<>(
            Boolean.class, "cancel_explosion");

    public static final MetaKey<Boolean> CANCEL_EXPLOSION_BLOCKS = new MetaKey<>(
            Boolean.class, "cancel_explosion_blocks");

    public static final MetaKey<Boolean> MAGIC_SPAWNED = new MetaKey<>(
            Boolean.class, "magicspawned");

    public static final MetaKey<Boolean> TRACKING = new MetaKey<>(
            Boolean.class, "tracking");

    public static final MetaKey<Boolean> NOSPLIT = new MetaKey<>(
            Boolean.class, "nosplit");
}
