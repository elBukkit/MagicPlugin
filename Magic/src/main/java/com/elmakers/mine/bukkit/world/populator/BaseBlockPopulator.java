package com.elmakers.mine.bukkit.world.populator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.MagicWorld;

public abstract class BaseBlockPopulator extends BlockPopulator {
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

    protected Logger getLogger() {
        return world.getLogger();
    }

    public static List<BaseBlockPopulator> loadPopulators(MagicWorld world, ConfigurationSection config) {
        ConfigurationSection populatorConfig = config.getConfigurationSection("populators");
        if (populatorConfig == null) {
            populatorConfig = config.getConfigurationSection("chunk_generate");
        }
        if (populatorConfig == null) {
            return loadFromList(world, ConfigurationUtils.getStringList(config, "populators"));
        }
        return loadFromSections(world, populatorConfig);
    }

    public static BaseBlockPopulator loadPopulator(MagicWorld world, String key, ConfigurationSection config) {
        MagicController controller = world.getController();
        if (config == null) {
            controller.getLogger().warning("Was expecting a properties section in world populators config for key '" + world.getName());
            return null;
        }
        if (!config.getBoolean("enabled", true)) {
            return null;
        }

        String className = config.getString("class");
        BaseBlockPopulator populator = BaseBlockPopulator.create(controller, className);
        if (populator != null) {
            if (populator.load(world, config)) {
                controller.info("Adding " + key + " populator to " + world.getName());
            } else {
                populator = null;
            }
        }
        if (populator == null) {
            controller.info("Skipping invalid " + key + " populator for " + world.getName());
        }
        return populator;
    }

    public static BaseBlockPopulator parsePopulator(MagicWorld world, ConfigurationSection config) {
        ConfigurationSection populatorConfig = config.getConfigurationSection("populator");
        if (populatorConfig == null) {
            return loadPopulator(world, populatorConfig.getString("populator", ""));
        }
        return loadPopulator(world, null, populatorConfig);
    }

    public static BaseBlockPopulator loadPopulator(MagicWorld world, String key) {
        MagicController controller = world.getController();
        if (key.isEmpty() || key.equals("none")) return null;
        ConfigurationSection populatorConfig = controller.getWorlds().getPopulatorConfig(key);
        if (populatorConfig == null) {
            controller.getLogger().warning("Invalid block populator: " + key);
            return null;
        }
        return loadPopulator(world, key, populatorConfig);
    }

    private static List<BaseBlockPopulator> loadFromSections(MagicWorld world, ConfigurationSection populatorConfigs) {
        List<BaseBlockPopulator> populators = new ArrayList<>();
        for (String key : populatorConfigs.getKeys(false)) {
            ConfigurationSection handlerConfig = populatorConfigs.getConfigurationSection(key);
            BaseBlockPopulator populator = loadPopulator(world, key, handlerConfig);
            if (populator != null) {
                populators.add(populator);
            }
        }
        return populators;
    }

    private static List<BaseBlockPopulator> loadFromList(MagicWorld world, List<String> populatorConfigs) {
        List<BaseBlockPopulator> populators = new ArrayList<>();
        if (populatorConfigs == null || populatorConfigs.isEmpty()) return populators;
        for (String key : populatorConfigs) {
            BaseBlockPopulator populator = loadPopulator(world, key);
            if (populator != null) {
                populators.add(populator);
            }
        }
        return populators;
    }

    @Nullable
    public static BaseBlockPopulator create(MageController controller, String className) {
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

        if (newObject == null || !(newObject instanceof BaseBlockPopulator)) {
            controller.getLogger().warning("Error loading block populator: " + className + ", does it extend MagicBlockPopulator?");
            return null;
        }

        return (BaseBlockPopulator)newObject;
    }

    protected boolean setBlockData(final LimitedRegion region, int x, int y, int z, @NotNull BlockData blockData) {
        if (!region.isInRegion(x, y, z)) {
            getController().info("Trying to set out-of-range block: " + x + "," + y + "," + z);
            return false;
        }
        region.setBlockData(x, y, z, blockData);
        return true;
    }

    protected int searchBlock(final WorldInfo worldInfo, final LimitedRegion region, int x, int y, int z, int maxSearch, int direction, boolean throughAir, boolean returnAir) {
        if (!region.isInRegion(x, y, z)) {
            return y;
        }
        int blockCount = 0;
        Material material = region.getType(x, y, z);
        while (((direction > 0 && y < worldInfo.getMaxHeight() - 1) || (direction < 0 && y > worldInfo.getMinHeight() + 1))
                && material.isAir() == throughAir
                && blockCount < maxSearch) {
            y += direction;
            blockCount++;
            material = region.getType(x, y, z);
        }
        return throughAir == returnAir ? y - direction : y;
    }

    protected int getTopBlock(final WorldInfo worldInfo, final LimitedRegion region, int x, int y, int z) {
        return searchBlock(worldInfo, region, x, y, z, worldInfo.getMaxHeight() - worldInfo.getMinHeight(), 1, false, false);
    }

    protected int getBottomBlock(final WorldInfo worldInfo, final LimitedRegion region, int x, int y, int z) {
        return searchBlock(worldInfo, region, x, y, z, worldInfo.getMaxHeight() - worldInfo.getMinHeight(), -1, true, false);
    }

    protected List<MaterialAndData> parseBlocks(ConfigurationSection config, String key) {
        return parseBlocks(config, key, null);
    }

    protected List<MaterialAndData> parseBlocks(ConfigurationSection config, String key, String defaultSet) {
        return world.getController().getWorlds().parseBlocks(world.getName(), config, key, defaultSet);
    }

    public String getPortalTargetWorld(Location location) {
        return null;
    }
}
