package com.elmakers.mine.bukkit.api.spell;

/**
 * The type of targeting this Spell should perform.
 *
 * <p>This driven by the "target" parameter in the spell configuration or
 * command-line parameters.
 */
public enum TargetType {
    NONE(false),
    BLOCK,
    ANY,
    OTHER,
    ANY_ENTITY,
    OTHER_ENTITY,
    SELF(false),
    SELECT(false),
    SELECT_ENTITY(false),
    LAST_DAMAGER(false),
    TOP_DAMAGER(false),
    DAMAGE_TARGET(false);

    private final boolean ranged;

    TargetType() {
        this.ranged = true;
    }

    TargetType(boolean ranged) {
        this.ranged = ranged;
    }

    @Deprecated
    public boolean targetsEntities() {
        return false;
    }

    public boolean isRanged() {
        return ranged;
    }
}
