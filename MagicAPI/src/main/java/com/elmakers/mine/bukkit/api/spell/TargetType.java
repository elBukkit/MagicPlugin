package com.elmakers.mine.bukkit.api.spell;

/**
 * The type of targeting this Spell should perform.
 *
 * <p>This driven by the "target" parameter in the spell configuration or
 * command-line parameters.
 */
public enum TargetType {
    NONE(false),
    BLOCK(false),
    ANY(true),
    OTHER(true),
    ANY_ENTITY(true),
    OTHER_ENTITY(true),
    SELF(false),
    SELECT(false),
    SELECT_ENTITY(true);

    private boolean targetEntities;

    TargetType(boolean targetEntities) {
        this.targetEntities = true;
    }

    public boolean targetsEntities() {
        return this.targetEntities;
    }
}
