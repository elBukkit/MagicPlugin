package com.elmakers.mine.bukkit.magic;

/**
 * Exception that is thrown when a mage could not be found nor loaded for a
 *  given entity or command sender.
 */
// TODO: Move to API
class NoSuchMageException extends RuntimeException {
    public NoSuchMageException(String mageId) {
        super("Failed to locate mage with id: " + mageId);
    }
}
