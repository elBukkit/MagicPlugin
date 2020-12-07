package com.elmakers.mine.bukkit.world;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.world.populator.MagicChunkHandler;
import com.elmakers.mine.bukkit.world.spawn.MagicSpawnHandler;

public class MagicWorld {
    private enum WorldState { UNLOADED, LOADING, LOADED }

    private final MagicController controller;
    private MagicChunkHandler chunkHandler;
    private MagicSpawnHandler spawnHandler;
    private String copyFrom;
    private boolean autoLoad = false;
    private World.Environment worldEnvironment = World.Environment.NORMAL;
    private World.Environment appearanceEnvironment = null;
    private WorldType worldType = WorldType.NORMAL;
    private String worldName;
    private long seed;
    private static Random random = new Random();
    private WorldState state = WorldState.UNLOADED;

    public MagicWorld(MagicController controller) {
        this.controller = controller;
    }

    public void load(String name, ConfigurationSection config) {
        worldName = name;
        copyFrom = config.getString("copy", "");
        if (config.contains("environment")) {
            String typeString = config.getString("environment");
            try {
                worldEnvironment = World.Environment.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid world environment: " + typeString);
            }
        }
        if (config.contains("appearance")) {
            String typeString = config.getString("appearance");
            try {
                appearanceEnvironment = World.Environment.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid world appearance: " + typeString);
            }
        }
        if (config.contains("type")) {
            String typeString = config.getString("type");
            try {
                worldType = WorldType.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid world type: " + typeString);
            }
        }
        seed = config.getLong("seed", random.nextLong());
        autoLoad = config.getBoolean("autoload", false);

        if (chunkHandler == null) {
            chunkHandler = new MagicChunkHandler(controller);
        }
        if (spawnHandler == null) {
            spawnHandler = new MagicSpawnHandler(controller);
        }
        chunkHandler.clear();
        ConfigurationSection chunkConfig = config.getConfigurationSection("chunk_generate");
        if (chunkConfig != null) {
            chunkHandler.load(worldName, chunkConfig);
        }

        spawnHandler.clear();
        ConfigurationSection entityConfig = config.getConfigurationSection("entity_spawn");
        if (entityConfig != null) {
            spawnHandler.load(worldName, entityConfig);
        }

        // Autoload worlds
        if (autoLoad && copyFrom.isEmpty()) {
            // Wait a few ticks to do this, to avoid errors during initialization
            Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
               @Override
               public void run()
               {
                  createWorld();
               }
           }, 1L);
        }
    }

    public void createWorld() {
        state = WorldState.LOADING;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            controller.getLogger().info("Loading " + worldName + " as " + worldEnvironment + " (" + worldType + ")");
            WorldCreator worldCreator = WorldCreator.name(worldName);
            worldCreator.seed(seed);
            worldCreator.environment(worldEnvironment);
            worldCreator.type(worldType);
            worldCreator.generateStructures(true);
            try {
                world = worldCreator.createWorld();
            } catch (Exception ex) {
                world = null;
                ex.printStackTrace();
            }
            if (world == null) {
                controller.getLogger().warning("Failed to create world: " + worldName);
            }
        }
        if (world != null && appearanceEnvironment != null) {
            NMSUtils.setEnvironment(world, appearanceEnvironment);
            controller.getLogger().info("Changed " + worldName + " appearance to " + appearanceEnvironment);
        }
    }

    public void finalizeLoad() {
        if (spawnHandler != null) {
            spawnHandler.finalizeLoad();
        }
    }

    public void installPopulators(World world) {
        if (chunkHandler.isEmpty()) return;
        controller.getLogger().info("Installing Populators in " + world.getName());
        world.getPopulators().add(chunkHandler);
    }

    @Nullable
    public LivingEntity processEntitySpawn(Plugin plugin, LivingEntity entity) {
        return spawnHandler.process(plugin, entity);
    }

    public void onWorldInit(final Plugin plugin, final World initWorld)
    {
        // Loaded check
        if (state != WorldState.UNLOADED) {
            return;
        }

        // Flag loaded worlds
        if (initWorld.getName().equals(worldName)) {
            state = WorldState.LOADED;
            return;
        }

        if (copyFrom.length() == 0 || !initWorld.getName().equals(copyFrom)) return;

        state = WorldState.LOADING;

        // Wait a few ticks to do this, to avoid errors during initialization
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
           @Override
           public void run()
           {
               // Create this world if it doesn't exist
               World world = Bukkit.getWorld(worldName);
               if (world == null) {
                   controller.getLogger().info("Loading " + worldName + " using settings copied from " + initWorld.getName());
                   world = Bukkit.createWorld(new WorldCreator(worldName).copy(initWorld));
                   if (world == null) {
                       controller.getLogger().warning("Failed to create world: " + worldName);
                   } else if (appearanceEnvironment != null) {
                       NMSUtils.setEnvironment(world, appearanceEnvironment);
                       controller.getLogger().info("Changed " + worldName + " appearance to " + appearanceEnvironment);
                   }
               }
           }
        }, 1);
    }
}
