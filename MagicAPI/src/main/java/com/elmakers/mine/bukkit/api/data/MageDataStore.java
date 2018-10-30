package com.elmakers.mine.bukkit.api.data;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;

public interface MageDataStore {
    /**
     * Implementing classes must have a default constructor.
     */

    /**
     * Initialize the data store. This will be called on load.
     * Any configuration parameters set in config.yml for this store
     * will be passed in to the ConfigurationSection here.
     */
    void initialize(MageController controller, ConfigurationSection configuration);

    /**
     * Save a single Mage.
     *
     * <p>If the provided callback is non-null, it should be called on completion.
     */
    @Deprecated
    void save(MageData mage, MageDataCallback callback);

    /**
     * Save a single Mage.
     *
     * <p>If the provided callback is non-null, it should be called on completion.
     */
    void save(MageData mage, MageDataCallback callback, boolean releaseLock);

    /**
     * Force-release a lock for a mage
     */
    void releaseLock(MageData mage);

    /**
     * Save several Mages in a batch.
     */
    void save(Collection<MageData> mages);

    /**
     * Load a single mage by id.
     *
     * <p>If there is no data for this mage, a new empty record should be returned.
     *
     * <p>If the provided callback is non-null, it should be called on completion.
     */
    void load(String id, MageDataCallback callback);

    /**
     * Remove all data for a single mage
     */
    void delete(String id);

    /**
     * Retrieve a list of all known Mage ids
     * This may be used in the future for auto-migration between
     * DataStores.
     */
    Collection<String> getAllIds();

    /**
     * Mark a Mage as having been migrated.
     *
     * <p>This may be a deletion, backup or flagging,
     * however the implementation prefers.
     */
    void migrate(String id);
}
