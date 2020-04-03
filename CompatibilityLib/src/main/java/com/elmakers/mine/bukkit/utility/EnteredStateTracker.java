package com.elmakers.mine.bukkit.utility;

/**
 * Tracks whether or not a certain code block has been entered or note.
 */
final class EnteredStateTracker {
    public class Touchable implements AutoCloseable {
        @Override
        public void close() {
            isInside = false;
        }

        /**
         * This is just here to avoid compiler warnings by giving the user something to call
         */
        public void touch() {

        }
    }

    private boolean isInside = false;
    private final Touchable close = new Touchable();

    public boolean isInside() {
        return isInside;
    }

    public Touchable enter() {
        isInside = true;
        return close;
    }
}
