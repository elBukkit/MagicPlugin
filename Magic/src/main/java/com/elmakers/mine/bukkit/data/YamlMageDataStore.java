package com.elmakers.mine.bukkit.data;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class YamlMageDataStore extends ConfigurationMageDataStore {
    private File playerDataFolder;
    private File migratedDataFolder;
    private final Map<String, FileLock> locks = new HashMap<>();

    @Override
    public void initialize(MageController controller, ConfigurationSection configuration) {
        super.initialize(controller, configuration);
        Plugin plugin = controller.getPlugin();
        String playerFolder = configuration.getString("folder", "players");
        String migrateFolder = configuration.getString("migration_folder", "migrated");
        playerDataFolder = new File(plugin.getDataFolder(), playerFolder);
        playerDataFolder.mkdirs();
        migratedDataFolder = new File(plugin.getDataFolder(), migrateFolder);
    }

    @Override
    public void save(MageData mage, MageDataCallback callback) {
        save(mage, callback, false);
    }

    @Override
    public void save(MageData mage, MageDataCallback callback, boolean releaseLock) {
        File playerData = new File(playerDataFolder, mage.getId() + ".dat");
        YamlDataFile saveFile = new YamlDataFile(controller.getLogger(), playerData);
        save(mage, saveFile);
        saveFile.save();
        if (releaseLock) {
            synchronized (locks) {
                FileLock lock = locks.remove(mage.getId());
                if (lock != null) {
                    try {
                        lock.release();
                        controller.info("Released file lock for " + mage.getId() + " at " + System.currentTimeMillis());
                    } catch (Exception ex) {
                        controller.getLogger().log(Level.WARNING, "Unable to release file lock for " + mage.getId(), ex);
                    }
                }
            }
        }

        if (callback != null) {
            callback.run(mage);
        }
    }

    protected void obtainLock(String id) {
        if (controller.isFileLockingEnabled()) {
            synchronized (locks) {
                if (locks.containsKey(id)) return;
                try {
                    final File lockFile = new File(playerDataFolder, id + ".lock");
                    RandomAccessFile file = new RandomAccessFile(lockFile, "rw");
                    FileChannel channel = file.getChannel();
                    controller.info("Obtaining lock for " + lockFile.getName() + " at " + System.currentTimeMillis());
                    FileLock lock = channel.lock();
                    controller.info("  Obtained lock for " + lockFile.getName() + " at " + System.currentTimeMillis());
                    locks.put(id, lock);
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Unable to obtain file lock for " + id, ex);
                }
            }
        }
    }

    @Override
    public void load(String id, MageDataCallback callback) {
        obtainLock(id);

        final File playerFile = new File(playerDataFolder, id + ".dat");
        if (!playerFile.exists()) {
            callback.run(null);
            return;
        }
        YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(playerFile);
        MageData data = load(id, saveFile);
        if (callback != null) {
            callback.run(data);
        }
    }

    @Override
    public void delete(String id) {
        File playerData = new File(playerDataFolder, id + ".dat");
        if (playerData.exists()) {
            playerData.delete();
        }
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

    @Override
    public void migrate(String id) {
        File playerData = new File(playerDataFolder, id + ".dat");
        if (playerData.exists()) {
            migratedDataFolder.mkdir();
            File migratedData = new File(migratedDataFolder, id + ".dat.migrated");
            playerData.renameTo(migratedData);
        }
    }
}
