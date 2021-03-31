package com.elmakers.mine.bukkit.api.spell;

/**
 * Every Spell will return a SpellResult when cast. This result
 * will determine the messaging and effects used, as well as whether
 * or not the Spell cast consumes its CastingCost costs.
 */
public enum SpellResult {
    // Order is important here

    // Will stop the current spell cast completely, returning success
    STOP(true, false, false, false, true),
    // Will pause the spell cast until the next tick
    PENDING(true, false, false, false, true),
    // General success
    CAST(true, false, false),
    // Success, but cast on self. Actions generally should not return this.
    CAST_SELF(true, false, false),
    // Success, but cast on a specific target. Actions generally should not return this.
    CAST_TARGET(true, false, false),
    // An alternate cast was performed. Actions generally should not return any of these.
    ALTERNATE(true, false, false, true),
    ALTERNATE_UP(true, false, false, true),
    ALTERNATE_DOWN(true, false, false, true),
    ALTERNATE_SNEAK(true, false, false, true),
    ALTERNATE_JUMPING(true, false, false, true),

    // Reactivated on reload
    REACTIVATE(true, false, true),

    // The spell cast fizzled. Actions generally should not return this.
    FIZZLE(false, true, false),
    // The spell cast backfired. Actions generally should not return this.
    BACKFIRE(false, true, false),
    // The spell cast was blocked. Actions generally should not return this.
    BLOCKED(false, true, false),

    // The spell cast was deactivated. Actions generally should not return this.
    DEACTIVATE(true, false, true),
    // A target was selected, which will be used in a subsequent cast. Actions generally should not return this.
    TARGET_SELECTED(false, false, true),

    // The spell cast was cancelled because the caster is cursed. Actions generally should not return this.
    CURSED(false, true, true),
    // The spell failed because it is on cooldown. Actions generally should not return this.
    COOLDOWN(false, true, true),

    // The spell missed, or did not have a valid target.
    NO_TARGET(false, false, false),

    // The spell cast failed due to some internal issue. It's rare that an action would return this.
    FAIL(false, true, true),
    // The spell cast failed due to not having sufficient casting costs. Actions generally should not return this.
    INSUFFICIENT_RESOURCES(false, true, true, false, true),
    // The spell cast failed due to no permissions (build, break, etc). Actions generally should not return this.
    INSUFFICIENT_PERMISSION(false, true, true),

    // The spell cast requires an entity but none was available.
    ENTITY_REQUIRED(false, true, true),
    // The spell cast requires a living entity but none was available.
    LIVING_ENTITY_REQUIRED(false, true, true),
    // The spell cast requires a player but none was available.
    PLAYER_REQUIRED(false, true, true),
    // The spell cast requires a location but none was available.
    LOCATION_REQUIRED(false, true, true),
    // The spell cast requires a world but none was available.
    WORLD_REQUIRED(false, true, true),
    // The spell cast requires a world but no valid world was provided.
    INVALID_WORLD(false, true, true),

    // Will stop the current spell cast, but refund casting costs
    CANCELLED(true, false, true, false, true),

    // This should always be last
    NO_ACTION(false, false, false);

    private final boolean success;
    private final boolean failure;
    private final boolean free;
    private final boolean alternate;
    private final boolean stop;

    SpellResult(boolean success, boolean failure, boolean free, boolean alternate, boolean stop) {
        this.success = success;
        this.failure = failure;
        this.free = free;
        this.alternate = alternate;
        this.stop = stop;
    }

    SpellResult(boolean success, boolean failure, boolean free) {
        this.success = success;
        this.failure = failure;
        this.free = free;
        this.alternate = false;
        this.stop = false;
    }

    SpellResult(boolean success, boolean failure, boolean free, boolean alternate) {
        this.success = success;
        this.failure = failure;
        this.free = free;
        this.alternate = alternate;
        this.stop = false;
    }

    /**
     * Determine if this result is a failure or not.
     *
     * <p>Note that a spell result can be neither failure nor
     * success.
     *
     * @return True if this cast was a failure.
     */
    public boolean isFailure() {
        return failure;
    }

    /**
     * Determine if this result is a success or not.
     *
     * @return True if this cast was a success.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Determine if this result is a success or not,
     * possibly counting no_target as a success.
     *
     * @return True if this cast was a success.
     */
    public boolean isSuccess(boolean castOnNoTarget) {
        if (this == SpellResult.NO_TARGET || this == SpellResult.NO_ACTION || this == SpellResult.STOP) {
            return castOnNoTarget;
        }
        return isSuccess();
    }

    /**
     * Determine if this result is a free cast or not.
     *
     * @return True if this cast should not consume costs.
     */
    public boolean isFree() {
        return free;
    }

    public boolean isFree(boolean castOnNoTarget) {
        if (this == SpellResult.NO_TARGET || this == SpellResult.NO_ACTION)
        {
            return !castOnNoTarget;
        }
        return isFree();
    }

    /**
     * Determine if this result should stop processing or not.
     *
     * @return True if this result should stop processing, either
     *      temporarily (PENDING) or permanently (CANCELLED)
     */
    public boolean isStop() {
        return stop;
    }

    public boolean shouldRefundCooldown(boolean castOnNoTarget) {
        return  (!castOnNoTarget && (this == SpellResult.NO_TARGET || this == SpellResult.NO_ACTION));
    }

    public boolean shouldRefundCosts(boolean castOnNoTarget, boolean refund) {
        return refund && !isFree(castOnNoTarget) && this != SpellResult.INSUFFICIENT_RESOURCES && (this == SpellResult.NO_TARGET || this == SpellResult.NO_ACTION);
    }

    /**
     * Determine if this result is an alternate-mode cast.
     *
     * @return True if this cast was an alternate-mode.
     */
    public boolean isAlternate() {
        return alternate;
    }

    public SpellResult min(SpellResult other) {
        return (this.ordinal() < other.ordinal()) ? this : other;
    }

    public SpellResult max(SpellResult other) {
        return (this.ordinal() > other.ordinal()) ? this : other;
    }
}
