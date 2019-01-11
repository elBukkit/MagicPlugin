package com.elmakers.mine.bukkit.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataSession;
import com.elmakers.mine.bukkit.api.data.MageDataStore;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public final class MageRepository {
    private final MagicController controller;

    private @Nullable MageDataStore mageDataStore = null;
    private boolean saveNonPlayerMages = false;
    private boolean savePlayerData = true;
    private boolean externalPlayerData = false;

    private final Map<String, MageDataSession> activeMages = new HashMap<>();

    public MageRepository(MagicController controller) {
        this.controller = controller;
    }

    public void loadProperties(ConfigurationSection properties) {
        saveNonPlayerMages = properties.getBoolean("save_non_player_mages",
                saveNonPlayerMages);
        savePlayerData = properties.getBoolean("save_player_data", true);
        externalPlayerData = properties.getBoolean("external_player_data",
                false);
        if (externalPlayerData) {
            controller.getLogger().info(
                    "Magic is expecting player data to be loaded from an external source");
        } else if (!savePlayerData) {
            controller.getLogger().info("Magic player data saving is disabled");
        }

        ConfigurationSection mageDataStore = properties
                .getConfigurationSection("player_data_store");
        if (mageDataStore != null) {
            String dataStoreClassName = mageDataStore.getString("class");
            try {
                Class<?> dataStoreClass = Class.forName(dataStoreClassName);
                Object dataStore = dataStoreClass.getDeclaredConstructor()
                        .newInstance();
                if (dataStore == null
                        || !(dataStore instanceof MageDataStore)) {
                    controller.getLogger().log(Level.WARNING,
                            "Invalid player_data_store class "
                                    + dataStoreClassName
                                    + ", does it implement MageDataStore? Player data saving is disabled!");
                    this.mageDataStore = null;
                } else {
                    this.mageDataStore = (MageDataStore) dataStore;
                    this.mageDataStore.initialize(controller, mageDataStore);
                }
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING,
                        "Failed to create player_data_store class from "
                                + dataStoreClassName
                                + " player data saving is disabled!",
                        ex);
                this.mageDataStore = null;
            }
        } else {
            controller.getLogger().log(Level.WARNING,
                    "Missing player_data_store configuration, player data saving disabled!");
            this.mageDataStore = null;
        }
    }

    private MageDataSession getLockedMage(String id) {
        return Verify.verifyNotNull(
                activeMages.get(id),
                "No open mage session for: %s", id);
    }

    private MageDataSession getLockedMage(String id, boolean releaseLock) {
        if (!releaseLock) {
            return getLockedMage(id);
        } else {
            return Verify.verifyNotNull(
                    activeMages.remove(id),
                    "No open mage session for: %s", id);
        }
    }

    public com.elmakers.mine.bukkit.magic.Mage loadOrCreateMage(
            @Nonnull String mageId, @Nullable String mageName,
            @Nullable CommandSender commandSender, @Nullable Entity entity) {
        boolean isPlayer = (entity instanceof Player)
                && !controller.isNPC(entity);

        if (isPlayer && !((Player) entity).isOnline()) {
            controller.getLogger().warning("Player data for " + mageId + " ("
                    + entity.getName() + ") loaded while offline!");
            Thread.dumpStack();

            controller.getLogger().warning(
                    "Returning dummy Mage to avoid locking issues");
            return new com.elmakers.mine.bukkit.magic.Mage(
                    mageId, controller);
        }

        com.elmakers.mine.bukkit.magic.Mage mage = new com.elmakers.mine.bukkit.magic.Mage(
                mageId, controller);

        mage.setName(mageName);
        mage.setCommandSender(commandSender);
        mage.setEntity(entity);
        if (entity instanceof Player) {
            mage.setPlayer((Player) entity);
        }

        if (mageDataStore == null) {
            mage.load(null);
            return mage;
        }

        if (savePlayerData) {
            if (isPlayer || saveNonPlayerMages) {
                loadMageData(mage);
            } else {
                mage.load(null);
            }
        } else if (externalPlayerData && (isPlayer || saveNonPlayerMages)) {
            mage.setLoading(true);
        } else {
            mage.load(null);
        }

        return mage;
    }

    private void loadMageData(com.elmakers.mine.bukkit.magic.Mage mage) {
        mage.setLoading(true);

        String id = mage.getId();
        controller.info("Loading mage data for "
                + mage.getName() + " ("
                + id + ") at "
                + System.currentTimeMillis());

        MageDataSession session = activeMages.get(id);
        ListenableFuture<MageData> future;
        if (session != null) {
            future = session.getData();
        } else {
            session = mageDataStore.openSession(id);
            future = session.getData();

            activeMages.put(id, session);
        }

        future.addListener(() -> {
            MageData data;
            try {
                data = future.get();
            } catch (InterruptedException | ExecutionException e) {
                controller.getLogger().log(
                        Level.SEVERE,
                        "Failed to load mage data for " + mage.getId(), e);
                return;
            }

            // TODO: Check if mage is still interested
            mage.load(data);
            controller.info(
                    " Finished Loading mage data for " + mage.getName() + " ("
                            + id + ") at " + System.currentTimeMillis());
        }, t -> Bukkit.getScheduler().runTask(controller.getPlugin(), t));
    }

    public ListenableFuture<MageData> saveMage(Mage mage) {
        return saveMage(mage, true);
    }

    public ListenableFuture<MageData> saveMage(Mage mage, boolean async) {
        return saveMage(mage, async, false, false);
    }

    public ListenableFuture<MageData> saveMage(
            Mage mage,
            boolean async,
            boolean wandInventoryOpen,
            boolean releaseLock) {
        if (!savePlayerData || mageDataStore == null) {
            return Futures.immediateCancelledFuture();
        }

        controller.info("Saving player data for " + mage.getName() + " ("
                + mage.getId()
                + ") at " + System.currentTimeMillis());

        MageData mageData = new MageData(mage.getId());
        if (!mage.save(mageData)) {
            return Futures.immediateCancelledFuture();
        }

        if (wandInventoryOpen) {
            mageData.setOpenWand(true);
        }

        MageDataSession session = getLockedMage(mage.getId(), releaseLock);
        ListenableFuture<Void> future = session.save(mageData);

        future.addListener(() -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                controller.getLogger().log(
                        Level.SEVERE,
                        "Failed to save mage data for mage " + mage.getId(),
                        e);
            }
        }, MoreExecutors.sameThreadExecutor());

        if (releaseLock) {
            return Futures.transform(
                    future,
                    (Void v) -> {
                        session.close();
                        return mageData;
                    });
        } else {
            return Futures.transform(future, (Void v) -> mageData);
        }
    }

    public boolean hasDataStore() {
        return mageDataStore != null;
    }

    public void delete(String id) {
        controller.info("Deleted mage id " + id);
        getLockedMage(id).reset();
    }

    public boolean shouldSavePlayerData() {
        return savePlayerData;
    }

    public boolean isExternalPlayerData() {
        return externalPlayerData;
    }

    public void managePlayerData(
            boolean external,
            boolean backupInventories) {
        savePlayerData = !external;
        externalPlayerData = external;
    }

    public boolean isSaveNonPlayerMages() {
        return saveNonPlayerMages;
    }
}
