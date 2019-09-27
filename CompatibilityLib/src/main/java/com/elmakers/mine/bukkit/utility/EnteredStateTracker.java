package com.elmakers.mine.bukkit.utility;

/**
 * Tracks whether or not a certain code block has been entered or note.
 */
final class EnteredStateTracker {
    private boolean isInside = false;
    private final AutoCloseable close = () -> isInside = false;

    public boolean isInside() {
        return isInside;
    }

    public AutoCloseable enter() {
        isInside = true;
        return close;
    }
}
