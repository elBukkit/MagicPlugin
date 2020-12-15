package com.elmakers.mine.bukkit.world.populator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicChunkHandler {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.populator.builtin";

    private final MagicController controller;
    private final List<MagicChunkPopulator> chunkPopulators = new ArrayList<MagicChunkPopulator>();

    public MagicChunkHandler(MagicController controller) {
        this.controller = controller;
    }

    public void load(String worldName, ConfigurationSection config) {
        for (String key : config.getKeys(false)) {
            ConfigurationSection handlerConfig = config.getConfigurationSection(key);
            if (handlerConfig == null) {
                controller.getLogger().warning("Was expecting a properties section in world chunk_generate config for key '" + worldName + "', but got: " + config.get(key));
                continue;
            }
            if (!handlerConfig.getBoolean("enabled", true)) {
                continue;
            }

            String className = handlerConfig.getString("class");
            MagicChunkPopulator populator = createChunkPopulator(className);
            if (populator != null) {
                if (populator.load(handlerConfig, controller)) {
                    chunkPopulators.add(populator);
                    controller.info("Adding " + key + " populator to " + worldName);
                } else {
                    controller.info("Skipping invalid " + key + " populator for " + worldName);
                }
            } else {
                controller.info("Skipping invalid " + key + " populator for " + worldName);
            }
        }
    }

    public Collection<MagicChunkPopulator> getPopulators() {
        return chunkPopulators;
    }

    @Nullable
    protected MagicChunkPopulator createChunkPopulator(String className) {
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
            controller.getLogger().log(Level.WARNING, "Error loading chunk populator: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading chunk populator: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof MagicChunkPopulator)) {
            controller.getLogger().warning("Error loading chunk populator: " + className + ", does it extend MagicChunkPopulator?");
            return null;
        }

        return (MagicChunkPopulator)newObject;
    }

    public boolean isEmpty() {
        return chunkPopulators.size() == 0;
    }
}
