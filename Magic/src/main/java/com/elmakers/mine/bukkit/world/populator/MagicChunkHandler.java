package com.elmakers.mine.bukkit.world.populator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicChunkHandler extends BlockPopulator {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.populator.builtin";

    private final MagicController controller;
    private final Map<String, MagicChunkPopulator> chunkPopulators = new HashMap<String, MagicChunkPopulator>();

    public MagicChunkHandler(MagicController controller) {
        this.controller = controller;
    }

    public void load(String worldName, ConfigurationSection config) {
        for (String key : config.getKeys(false)) {
            ConfigurationSection handlerConfig = config.getConfigurationSection(key);
            if (handlerConfig == null) continue;

            String className = handlerConfig.getString("class");
            MagicChunkPopulator populator = chunkPopulators.get(key);
            if (populator == null) {
                populator = createChunkPopulator(className);
            }
            if (populator != null) {
                if (populator.load(handlerConfig, controller)) {
                    chunkPopulators.put(key, populator);
                    controller.getLogger().info("Adding " + key + " populator to " + worldName);
                } else {
                    controller.getLogger().info("Skipping invalid " + key + " populator for " + worldName);
                }
            }
        }
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (MagicChunkPopulator populator : chunkPopulators.values()) {
            populator.populate(world, random, chunk);
        }
    }

    public void clear() {
        chunkPopulators.clear();
    }

    @Nullable
    protected MagicChunkPopulator createChunkPopulator(String className) {
        if (className == null) return null;

        if (className.indexOf('.') <= 0) {
            className = BUILTIN_CLASSPATH + "." + className;
        }

        Class<?> handlerClass = null;
        try {
            handlerClass = Class.forName(className);
        } catch (Throwable ex) {
            controller.getLogger().warning("Error loading chunk populator: " + className);
            ex.printStackTrace();
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.newInstance();
        } catch (Throwable ex) {
            controller.getLogger().warning("Error loading chunk populator: " + className);
            ex.printStackTrace();
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
