package com.elmakers.mine.bukkit.api.data;

import javax.annotation.Nonnull;

import com.google.common.util.concurrent.ListenableFuture;

public interface MageDataSession {
    /**
     * @return The most recently saved data for this mage.
     */
    ListenableFuture<MageData> getData();

    /**
     * Updates the most recent mage data and schedules a save.
     *
     * <p>The data will be available through {@link #getData()} even if the save
     * has not completed yet.
     *
     * @param data
     *            The data to write.
     * @param canBeAsync
     *            Whether the save may beformed asynchroniously. When this is
     *            false the save must be comitted before this method returns.
     * @return The future describing the status of the save.
     */
    ListenableFuture<Void> save(@Nonnull MageData data, boolean canBeAsync);

    default ListenableFuture<Void> save(@Nonnull MageData data) {
        return save(data, true);
    }

    /**
     * Clears the mage data of the mage, effectively resetting the mage to a
     * player that has not previously played yet.
     */
    ListenableFuture<Void> reset();

    /**
     * Closes the session, releasing the locks.
     */
    ListenableFuture<Void> close();
}
