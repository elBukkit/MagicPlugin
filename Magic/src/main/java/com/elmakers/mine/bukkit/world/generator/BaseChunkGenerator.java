package com.elmakers.mine.bukkit.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.WorldInfoContext;
import com.elmakers.mine.bukkit.world.biomes.SingleBiomeProvider;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public abstract class BaseChunkGenerator extends ChunkGenerator {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.generator.builtin";

    protected MagicWorld world;
    private String key;
    private ConfigurationSection configuration;
    private int yOffset = 0;
    private BiomeProvider biomeProvider;
    private List<BaseBlockPopulator> populators = new ArrayList<>();
    private PassthroughBlockPopulator passthroughPopulator = new PassthroughBlockPopulator(this);

    @Nullable
    public static BaseChunkGenerator create(MagicWorld world, String key, ConfigurationSection config) {
        String className = config.getString("class");
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
            world.getLogger().log(Level.WARNING, "Error loading chunk generator: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            world.getLogger().log(Level.WARNING, "Error loading chunk generator: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof BaseChunkGenerator)) {
            world.getLogger().warning("Error loading chunk generator " + key + " of class: " + className + ", does it extend MagicChunkGenerator?");
            return null;
        }

        BaseChunkGenerator generator = (BaseChunkGenerator)newObject;
        generator.key = key;
        generator.configuration = config;
        if (!generator.load(world, config)) {
            world.getLogger().warning("Invalid configuration for chunk generator: " + key);
            generator = null;
        }

        return generator;
    }

    public boolean load(MagicWorld world, ConfigurationSection configuration) {
        this.world = world;
        biomeProvider = createDefaultBiomeProvider(configuration);
        populators = BaseBlockPopulator.loadPopulators(world, configuration);
        yOffset = configuration.getInt("y_offset", 0);
        passthroughPopulator.setYOffset(yOffset);
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

    protected Logger getLogger() {
        return world.getLogger();
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

    public void populate(PopulatorContext context) {
        for (BaseBlockPopulator populator : populators) {
            // TODO: Pass in context
            populator.populate(context.getWorldInfo(), context.getRandom(), context.getChunkX(), context.getChunkZ(), context.getRegion());
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

    public void reload() {
        if (key != null) {
            world.getController().getWorlds().reloadGenerator(world, this, key);
        } else if (configuration != null) {
            load(world, configuration);
        }
    }

    @Override
    public final boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        final WorldContext context = new WorldInfoContext(worldInfo, random, chunkX, chunkZ);
        return shouldGenerateNoise(context);
    }

    public boolean shouldGenerateNoise(@NotNull WorldContext context) {
        return false;
    }

    @Override
    public final boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        final WorldContext context = new WorldInfoContext(worldInfo, random, chunkX, chunkZ);
        return shouldGenerateSurface(context);
    }

    public boolean shouldGenerateSurface(@NotNull WorldContext context) {
        return false;
    }

    @Override
    public final boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        final WorldContext context = new WorldInfoContext(worldInfo, random, chunkX, chunkZ);
        return shouldGenerateCaves(context);
    }

    public boolean shouldGenerateCaves(@NotNull WorldContext context) {
        return false;
    }

    @Override
    public final boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        final WorldContext context = new WorldInfoContext(worldInfo, random, chunkX, chunkZ);
        return shouldGenerateDecorations(context);
    }

    public boolean shouldGenerateDecorations(@NotNull WorldContext context) {
        return false;
    }

    @Override
    public final boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        final WorldContext context = new WorldInfoContext(worldInfo, random, chunkX, chunkZ);
        return shouldGenerateMobs(context);
    }

    public boolean shouldGenerateMobs(@NotNull WorldContext context) {
        return false;
    }

    @Override
    public final boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        final WorldContext context = new WorldInfoContext(worldInfo, random, chunkX, chunkZ);
        return shouldGenerateStructures(context);
    }

    public boolean shouldGenerateStructures(@NotNull WorldContext context) {
        return false;
    }

    @Override
    public final void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final GeneratorContext context = new GeneratorContext(worldInfo, random, chunkX, chunkZ, chunk, yOffset);
        generate(context);
    }

    public abstract boolean onLoad(ConfigurationSection configuration);
    public abstract void generate(final GeneratorContext context);
}
