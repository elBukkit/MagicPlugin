package com.elmakers.mine.bukkit.world.populator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.MagicWorld;

public abstract class MagicBlockPopulator extends BlockPopulator {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.populator.builtin";

    protected MagicWorld world;

    public boolean load(MagicWorld world, ConfigurationSection config) {
        this.world = world;
        return onLoad(config);
    }

    public abstract boolean onLoad(ConfigurationSection config);

    protected void logBlockRule(String message) {
        getController().info(message);
    }

    public MagicController getController() {
        return world.getController();
    }

    public static List<MagicBlockPopulator> loadPopulators(MagicWorld world, ConfigurationSection config) {
        ConfigurationSection populatorConfig = config.getConfigurationSection("populators");
        if (populatorConfig == null) {
            populatorConfig = config.getConfigurationSection("chunk_generate");
        }
        if (populatorConfig == null) {
            return loadFromList(world, ConfigurationUtils.getStringList(config, "populators"));
        }
        return loadFromSections(world, populatorConfig);
    }

    private static List<MagicBlockPopulator> loadFromSections(MagicWorld world, ConfigurationSection populatorConfigs) {
        List<MagicBlockPopulator> populators = new ArrayList<>();
        MagicController controller = world.getController();
        for (String key : populatorConfigs.getKeys(false)) {
            ConfigurationSection handlerConfig = populatorConfigs.getConfigurationSection(key);
            if (handlerConfig == null) {
                controller.getLogger().warning("Was expecting a properties section in world populators config for key '" + world.getName() + "', but got: " + populatorConfigs.get(key));
                continue;
            }
            if (!handlerConfig.getBoolean("enabled", true)) {
                continue;
            }

            String className = handlerConfig.getString("class");
            MagicBlockPopulator populator = MagicBlockPopulator.create(controller, className);
            if (populator != null) {
                if (populator.load(world, handlerConfig)) {
                    populators.add(populator);
                    controller.info("Adding " + key + " populator to " + world.getName());
                } else {
                    controller.info("Skipping invalid " + key + " populator for " + world.getName());
                }
            } else {
                controller.info("Skipping invalid " + key + " populator for " + world.getName());
            }
        }
        return populators;
    }

    private static List<MagicBlockPopulator> loadFromList(MagicWorld world, List<String> populatorConfigs) {
        List<MagicBlockPopulator> populators = new ArrayList<>();
        if (populatorConfigs == null || populatorConfigs.isEmpty()) return populators;
        MagicController controller = world.getController();
        for (String key : populatorConfigs) {
            ConfigurationSection populatorConfig = world.getController().getWorlds().getPopulatorConfig(key);
            if (populatorConfig == null) {
                controller.getLogger().warning("Invalid block populator: " + key);
                return null;
            }
            final String populatorClass = populatorConfig.getString("class");
            MagicBlockPopulator populator = MagicBlockPopulator.create(controller, populatorClass);
            if (populator == null) {
                controller.getLogger().warning("Invalid chunk generator class: " + populatorClass);
            } else {
                if (!populator.load(world, populatorConfig)) {
                    populator = null;
                }
            }
            if (populator != null) {
                populators.add(populator);
                world.getController().info("Adding " + key + " populator to " + world.getName());
            } else {
                world.getController().info("Skipping invalid " + key + " populator for " + world.getName());
            }
        }
        return populators;
    }

    @Nullable
    public static MagicBlockPopulator create(MageController controller, String className) {
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
            controller.getLogger().log(Level.WARNING, "Error loading block populator: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading block populator: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof MagicBlockPopulator)) {
            controller.getLogger().warning("Error loading block populator: " + className + ", does it extend MagicBlockPopulator?");
            return null;
        }

        return (MagicBlockPopulator)newObject;
    }

    protected int getTopBlock(final WorldInfo worldInfo, final LimitedRegion region, int x, int y, int z) {
        Material material = region.getType(x, y, z);
        while (y < worldInfo.getMaxHeight() && !material.isAir()) {
            y++;
            material = region.getType(x, y, z);
        }
        return y - 1;
    }
}
