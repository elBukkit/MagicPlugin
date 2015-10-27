package com.elmakers.mine.bukkit.data;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utilities.YamlDataFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collection;

public class YamlMageDataStore extends ConfigurationMageDataStore {
    private File playerDataFolder;

    @Override
    public void initialize(MageController controller, ConfigurationSection configuration) {
        super.initialize(controller, configuration);
        Plugin plugin = controller.getPlugin();
        String playerFolder = configuration.getString("folder", "players");
        playerDataFolder = new File(plugin.getDataFolder(), playerFolder);
        playerDataFolder.mkdirs();
    }

    @Override
    public void save(MageData mage) {
        File playerData = new File(playerDataFolder, mage.getId() + ".dat");
        YamlDataFile saveFile = new YamlDataFile(controller.getLogger(), playerData);
        save(mage, saveFile);
        saveFile.save();
    }

    @Override
    public void save(Collection<MageData> mages) {
        for (MageData data : mages) {
            save(data);
        }
    }

    @Override
    public MageData load(String id) {
        final File playerFile = new File(playerDataFolder, id + ".dat");
        if (!playerFile.exists()) return null;

        YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(playerFile);

        return load(id, saveFile);
    }

    @Override
    public void delete(String id) {
        // TODO
    }

    @Override
    public Collection<String> getAllIds() {
        // TODO
        return null;
    }

    @Override
    public void migrate(String id) {
        // TODO
    }
}
