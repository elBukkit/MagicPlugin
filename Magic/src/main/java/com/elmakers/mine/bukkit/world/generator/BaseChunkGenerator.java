package com.elmakers.mine.bukkit.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.biomes.SingleBiomeProvider;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public abstract class BaseChunkGenerator extends ChunkGenerator {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.generator.builtin";

    protected MagicWorld world;
    private BiomeProvider biomeProvider;
    private List<BaseBlockPopulator> populators = new ArrayList<>();
    private BlockPopulator passthroughPopulator = new PassthroughBlockPopulator(this);

    @Nullable
    public static BaseChunkGenerator create(MageController controller, String className) {
        if (className == null) {
            className = "Void";
        }

        if (className.indexOf('.') <= 0) {
            className = BUILTIN_CLASSPATH + "." + className;
            if (!className.endsWith("Generator")) {
                className += "Generator";
            }
        }

        Class<?> handlerClass = null;
        try {
            handlerClass = Class.forName(className);
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading chunk generator: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading chunk generator: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof BaseChunkGenerator)) {
            controller.getLogger().warning("Error loading chunk generator: " + className + ", does it extend MagicChunkGenerator?");
            return null;
        }

        return (BaseChunkGenerator)newObject;
    }

    public abstract boolean onLoad(ConfigurationSection configuration);

    public boolean load(MagicWorld world, ConfigurationSection configuration) {
        this.world = world;
        biomeProvider = createDefaultBiomeProvider(configuration);
        populators = BaseBlockPopulator.loadPopulators(world, configuration);
        return onLoad(configuration);
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return biomeProvider;
    }

    protected BiomeProvider createDefaultBiomeProvider(ConfigurationSection config) {
        String biomeKey = config.getString("biome");
        try {
            if (biomeKey != null) {
                return new SingleBiomeProvider(Biome.valueOf(biomeKey.toUpperCase(Locale.ROOT)));
            }
        } catch (Exception ex) {
            world.getLogger().warning("Invalid biome specified in " + world.getName() + " config: " + biomeKey);
        }
        return null;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return List.of(passthroughPopulator);
    }

    protected MagicController getController() {
        return world.getController();
    }

    protected Plugin getPlugin() {
        return world.getController().getPlugin();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return this.world.getSpawnLocation(world);
    }

    protected List<MaterialAndData> parseBlocks(ConfigurationSection config, String key) {
        return parseBlocks(config, key, null);
    }

    protected List<MaterialAndData> parseBlocks(ConfigurationSection config, String key, String defaultSet) {
        return world.getController().getWorlds().parseBlocks(world.getName(), config, key, defaultSet);
    }

    protected int getTopBlock(final ChunkData chunk, int x, int y, int z) {
        Material material = chunk.getType(x, y, z);
        while (y < chunk.getMaxHeight() && !material.isAir()) {
            y++;
            material = chunk.getType(x, y, z);
        }
        return y - 1;
    }

    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        for (BaseBlockPopulator populator : populators) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    public String getPortalTargetWorld(Location location) {
        for (BaseBlockPopulator populator : populators) {
            String populatorTarget = populator.getPortalTargetWorld(location);
            if (populatorTarget != null) {
                return populatorTarget;
            }
        }
        return null;
    }
}
