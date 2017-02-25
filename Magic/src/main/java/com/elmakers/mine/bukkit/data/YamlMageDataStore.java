package com.elmakers.mine.bukkit.data;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YamlMageDataStore extends ConfigurationMageDataStore {
    private File playerDataFolder;
    private File migratedDataFolder;

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
        File playerData = new File(playerDataFolder, mage.getId() + ".dat");
        YamlDataFile saveFile = new YamlDataFile(controller.getLogger(), playerData);
        save(mage, saveFile);
        saveFile.save();
        if (callback != null) {
            callback.run(mage);
        }
    }

    @Override
    public void load(String id, MageDataCallback callback) {
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
