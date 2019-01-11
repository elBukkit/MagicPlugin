package com.elmakers.mine.bukkit.api.data;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;

// NOTE: All methods should be called from the main thread, data stores should
// handle async themselves
public interface MageDataStore {
    /**
     * Implementing classes must have a default constructor.
     */

    /**
     * Initialize the data store. This will be called on load. Any configuration
     * parameters set in config.yml for this store will be passed in to the
     * ConfigurationSection here.
     */
    void initialize(MageController controller,
            ConfigurationSection configuration);

    /**
     * Opens a mage data session.
     *
     * <p>Implementations should handle locking, blocking and timeouts themselves.
     *
     * @param id
     *            The mage identifier.
     * @return The data session. Errors that occur while opening the sessions
     *         are returned via {@link MageDataSession#getData()}.
     */
    MageDataSession openSession(String id);

    // NOTE: May block
    Collection<String> getAllIds() throws UnsupportedOperationException;
}
