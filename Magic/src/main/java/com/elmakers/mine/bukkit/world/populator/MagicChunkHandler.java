package com.elmakers.mine.bukkit.world.populator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;

public class MagicChunkHandler {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.populator.builtin";

    private final MagicWorld world;
    private final List<MagicBlockPopulator> chunkPopulators = new ArrayList<>();

    public MagicChunkHandler(MagicWorld world) {
        this.world = world;
    }

    public void load(String worldName, ConfigurationSection config) {
        if (config == null) return;
        for (String key : config.getKeys(false)) {
            ConfigurationSection handlerConfig = config.getConfigurationSection(key);
            if (handlerConfig == null) {
                getController().getLogger().warning("Was expecting a properties section in world chunk_generate config for key '" + worldName + "', but got: " + config.get(key));
                continue;
            }
            if (!handlerConfig.getBoolean("enabled", true)) {
                continue;
            }

            String className = handlerConfig.getString("class");
            MagicBlockPopulator populator = createBlockPopulator(className);
            if (populator != null) {
                if (populator.load(handlerConfig, world)) {
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

    public Collection<MagicBlockPopulator> getPopulators() {
        return chunkPopulators;
    }

    @Nullable
    protected MagicBlockPopulator createBlockPopulator(String className) {
        if (className == null) return null;

        if (className.indexOf('.') <= 0) {
            className = BUILTIN_CLASSPATH + "." + className;
            if (!className.endsWith("Populator")) {
                className += "Populator";
            }
        }

        Class<?> handlerClass = null;
        try {
            handlerClass = Class.forName(className);
        } catch (Throwable ex) {
            getController().getLogger().log(Level.WARNING, "Error loading chunk populator: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            getController().getLogger().log(Level.WARNING, "Error loading chunk populator: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof MagicBlockPopulator)) {
            getController().getLogger().warning("Error loading chunk populator: " + className + ", does it extend MagicChunkPopulator?");
            return null;
        }

        return (MagicBlockPopulator)newObject;
    }

    public boolean isEmpty() {
        return chunkPopulators.size() == 0;
    }

    public MagicController getController() {
        return world.getController();
    }
}
