package com.elmakers.mine.bukkit.world.populator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;

public class MagicChunkHandler {

    private final MagicWorld world;
    private final List<MagicBlockPopulator> chunkPopulators = new ArrayList<>();

    public MagicChunkHandler(MagicWorld world) {
        this.world = world;
    }

    public void load(String worldName, ConfigurationSection populatorConfigs) {
        if (populatorConfigs == null) return;
        for (String key : populatorConfigs.getKeys(false)) {
            ConfigurationSection handlerConfig = populatorConfigs.getConfigurationSection(key);
            if (handlerConfig == null) {
                getController().getLogger().warning("Was expecting a properties section in world populators config for key '" + worldName + "', but got: " + populatorConfigs.get(key));
                continue;
            }
            if (!handlerConfig.getBoolean("enabled", true)) {
                continue;
            }

            String className = handlerConfig.getString("class");
            MagicBlockPopulator populator = MagicBlockPopulator.create(getController(), className);
            if (populator != null) {
                populator.setKey(key);
                if (populator.load(world, handlerConfig)) {
                    chunkPopulators.add(populator);
                    getController().info("Adding " + key + " populator to " + worldName);
                } else {
                    getController().info("Skipping invalid " + key + " populator for " + worldName);
                }
            } else {
                getController().info("Skipping invalid " + key + " populator for " + worldName);
            }
        }
    }

    public void load(String worldName, List<String> populators) {
        if (populators == null || populators.isEmpty()) return;
        for (String key : populators) {
            MagicBlockPopulator populator = world.getController().getWorlds().createPopulator(world, key);
            if (populator != null) {
                chunkPopulators.add(populator);
                getController().info("Adding " + key + " populator to " + worldName);
            } else {
                getController().info("Skipping invalid " + key + " populator for " + worldName);
            }
        }
    }

    public Collection<MagicBlockPopulator> getPopulators() {
        return chunkPopulators;
    }

    public boolean isEmpty() {
        return chunkPopulators.size() == 0;
    }

    public MagicController getController() {
        return world.getController();
    }
}
