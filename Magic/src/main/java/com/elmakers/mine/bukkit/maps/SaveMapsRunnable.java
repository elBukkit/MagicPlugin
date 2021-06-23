package com.elmakers.mine.bukkit.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

class SaveMapsRunnable implements Runnable {
    private final MapController mapController;
    final List<URLMap> saveMaps;

    public SaveMapsRunnable(MapController mapController, Collection<URLMap> maps) {
        this.mapController = mapController;
        saveMaps = new ArrayList<>(maps);
    }

    @Override
    public void run() {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            for (URLMap map : saveMaps) {
                ConfigurationSection mapConfig = configuration.createSection(Integer.toString(map.id));
                mapConfig.set("world", map.world);
                mapConfig.set("url", map.url);
                mapConfig.set("x", map.x);
                mapConfig.set("y", map.y);
                mapConfig.set("width", map.width);
                mapConfig.set("height", map.height);
                mapConfig.set("enabled", map.isEnabled());
                mapConfig.set("name", map.name);
                mapConfig.set("player", map.playerName);
                if (map.priority != null) {
                    mapConfig.set("priority", map.priority);
                }
                if (map.xOverlay != null) {
                    mapConfig.set("x_overlay", map.xOverlay);
                }
                if (map.yOverlay != null) {
                    mapConfig.set("y_overlay", map.yOverlay);
                }
            }
            File tempFile = new File(mapController.configurationFile.getAbsolutePath() + ".tmp");
            configuration.save(tempFile);
            if (mapController.configurationFile.exists()) {
                File backupFile = new File(mapController.configurationFile.getAbsolutePath() + ".bak");
                if (!backupFile.exists() || mapController.configurationFile.length() >= backupFile.length()) {
                    if (backupFile.exists() && !backupFile.delete()) {
                        mapController.warning("Failed to delete backup file in order to replace it: " + backupFile.getAbsolutePath());
                    }
                    mapController.configurationFile.renameTo(backupFile);
                } else {
                    mapController.info("Backup file is larger than current map file, you may want to restore or delete it? " + backupFile.getAbsolutePath());
                    if (!mapController.configurationFile.delete()) {
                        mapController.warning("Failed to delete file in order to replace it: " + mapController.configurationFile.getAbsolutePath());
                    }
                }
            }

            if (!tempFile.renameTo(mapController.configurationFile)) {
                mapController.warning("Failed to rename file from " + tempFile.getAbsolutePath() + " to " + mapController.configurationFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            mapController.warning("Failed to save file " + mapController.configurationFile.getAbsolutePath());
        }
        mapController.saveTask = null;
    };
}
