package com.elmakers.mine.bukkit.world.generator;

import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.biomes.SingleBiomeProvider;

public abstract class MagicChunkGenerator extends ChunkGenerator {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.generator.builtin";

    private MagicWorld world;
    private BiomeProvider biomeProvider;

    public abstract int getGroundLevel();

    public abstract int getBedrockLevel();

    public abstract Location getSpawnLocation(World world);

    @Nullable
    public static MagicChunkGenerator create(MageController controller, String className) {
        if (className == null) return null;

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

        if (newObject == null || !(newObject instanceof MagicChunkGenerator)) {
            controller.getLogger().warning("Error loading chunk generator: " + className + ", does it extend MagicChunkGenerator?");
            return null;
        }

        return (MagicChunkGenerator)newObject;
    }

    public void load(MagicWorld world, ConfigurationSection configuration) {
        this.world = world;
        biomeProvider = createDefaultBiomeProvider(configuration);
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

    protected Plugin getPlugin() {
        return world.getController().getPlugin();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return getSpawnLocation(world);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
