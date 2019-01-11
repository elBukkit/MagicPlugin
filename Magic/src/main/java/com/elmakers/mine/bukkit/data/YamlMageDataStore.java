package com.elmakers.mine.bukkit.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataSession;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public class YamlMageDataStore extends ConfigurationMageDataStore {
    private File playerDataFolder;
    private boolean asynchronousSaving = true;
    private boolean isFileLockingEnabled = false;
    private int fileLoadDelay = 0;

    @Override
    public void initialize(
            MageController controller,
            ConfigurationSection configuration) {
        super.initialize(controller, configuration);
        Plugin plugin = controller.getPlugin();

        String playerFolder = configuration.getString("folder", "players");
        playerDataFolder = new File(plugin.getDataFolder(), playerFolder);
        playerDataFolder.mkdirs();

        asynchronousSaving = configuration.getBoolean(
                "save_player_data_asynchronously", true);
        isFileLockingEnabled = configuration.getBoolean(
                "use_file_locking", false);
        fileLoadDelay = configuration.getInt(
                "file_load_delay", 0);
    }

    @Override
    public MageDataSession openSession(String id) {
        return new MageDataSession() {
            RandomAccessFile file;
            FileLock lock;
            ListenableFuture<MageData> data;

            {
                SettableFuture<MageData> data = SettableFuture.create();
                this.data = data;

                controller.getPlugin().getServer().getScheduler()
                        .runTaskLaterAsynchronously(controller.getPlugin(),
                                () -> {
                                    doLoad(data);
                                }, fileLoadDelay * 20 / 1000);
            }

            private void doLoad(SettableFuture<MageData> data) {
                if (isFileLockingEnabled) {
                    try {
                        File lockFile = new File(
                                playerDataFolder, id + ".lock");
                        file = new RandomAccessFile(lockFile, "rw");
                        FileChannel channel = file.getChannel();
                        controller
                                .info("Obtaining lock for " + lockFile.getName()
                                        + " at " + System.currentTimeMillis());
                        lock = channel.lock();
                        controller.info(
                                "  Obtained lock for " + lockFile.getName()
                                        + " at " + System.currentTimeMillis());
                    } catch (IOException ex) {
                        controller.getLogger().log(Level.WARNING,
                                "Unable to obtain file lock for " + id, ex);
                        data.setException(ex);
                        return;
                    }
                }

                File playerFile = new File(playerDataFolder, id + ".dat");
                if (!playerFile.exists()) {
                    data.set(null);
                } else {
                    YamlConfiguration saveFile = YamlConfiguration
                            .loadConfiguration(playerFile);
                    data.set(load(id, saveFile));
                }
            }

            @Override
            public ListenableFuture<MageData> getData() {
                return data;
            }

            @Override
            public ListenableFuture<Void> save(MageData data, boolean canBeAsync) {
                SettableFuture<Void> future = SettableFuture.create();

                if (canBeAsync && asynchronousSaving) {
                    Bukkit.getScheduler().runTaskAsynchronously(
                            controller.getPlugin(),
                            () -> {
                                doSave(data, future);
                            });
                } else {
                    doSave(data, future);
                }

                return future;
            }

            private void doSave(MageData data, SettableFuture<Void> future) {
                try {
                    File playerData = new File(
                            playerDataFolder,
                            data.getId() + ".dat");
                    YamlDataFile saveFile = new YamlDataFile(
                            controller.getLogger(),
                            playerData);
                    ConfigurationMageDataStore.save(
                            controller, data, saveFile);
                    saveFile.save();

                    future.set(null);
                } catch (Exception e) {
                    future.setException(e);
                }
            }

            @Override
            public ListenableFuture<Void> reset() {
                File playerData = new File(playerDataFolder, id + ".dat");
                if (playerData.exists()) {
                    playerData.delete();
                }

                return Futures.immediateFuture(null);
            }

            @Override
            public ListenableFuture<Void> close() {
                SettableFuture<Void> future = SettableFuture.create();

                // We need to wait for the read to complete before we unlock
                // TODO: Wait for writes
                data.addListener(() -> {

                    if (lock != null) {
                        try {
                            lock.release();
                            controller.info(
                                    "Released file lock for " + id
                                            + " at "
                                            + System.currentTimeMillis());
                        } catch (Exception e) {
                            future.setException(e);
                            controller.getLogger().log(
                                    Level.WARNING,
                                    "Unable to release file lock for " + id,
                                    e);
                        }
                    }

                    if (file != null) {
                        try {
                            file.close();
                        } catch (IOException e) {
                            future.setException(e);
                        }
                    }
                }, MoreExecutors.sameThreadExecutor());

                return Futures.immediateFuture(null);
            }
        };
    }

    @Override
    public Collection<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        File[] files = playerDataFolder.listFiles();
        for (File file : files) {
            String filename = file.getName();
            int extensionIndex = filename.lastIndexOf('.');
            if (extensionIndex > 0) {
                filename = filename.substring(0, filename.lastIndexOf('.'));
            }

            ids.add(filename);
        }
        return ids;
    }
}
