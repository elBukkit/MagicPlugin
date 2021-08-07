package com.elmakers.mine.bukkit.world;

import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.world.block.MagicBlockHandler;
import com.elmakers.mine.bukkit.world.populator.MagicChunkHandler;
import com.elmakers.mine.bukkit.world.spawn.MagicSpawnHandler;
import com.elmakers.mine.bukkit.world.tasks.CheckWorldCreateTask;
import com.elmakers.mine.bukkit.world.tasks.CopyWorldTask;

public class MagicWorld {
    private enum WorldState { UNLOADED, LOADING, LOADED }

    private static boolean updatingTime = false;

    private final MagicController controller;
    private final MagicChunkHandler chunkHandler;
    private final MagicSpawnHandler spawnHandler;
    private final MagicBlockHandler blockPlaceHandler;
    private final MagicBlockHandler blockBreakHandler;
    private String copyFrom = "";
    private boolean autoLoad = false;
    private boolean cancelSpellsOnSave = true;
    private World.Environment worldEnvironment = World.Environment.NORMAL;
    private World.Environment appearanceEnvironment = null;
    private WorldType worldType = WorldType.NORMAL;
    private String worldName;
    private String resourcePack;
    private long seed;
    private boolean synchronizeTime = true;
    private boolean installed = false;
    private long synchronizedTimeOffset = 0;
    private static Random random = new Random();
    private WorldState state = WorldState.UNLOADED;
    private Integer maxHeight;
    private Integer minHeight;
    private GameMode gameMode;
    private GameMode leavingGameMode;

    public MagicWorld(MagicController controller) {
        this.controller = controller;
        seed = random.nextLong();
        chunkHandler = new MagicChunkHandler(controller);
        spawnHandler = new MagicSpawnHandler(controller);
        blockPlaceHandler = new MagicBlockHandler(controller);
        blockBreakHandler = new MagicBlockHandler(controller);
    }

    public void load(String name, ConfigurationSection config) {
        worldName = name;
        if (config.contains("max_height")) {
            maxHeight = config.getInt("max_height");
        }
        if (config.contains("min_height")) {
            minHeight = config.getInt("min_height");
        }
        copyFrom = config.getString("copy", copyFrom);
        synchronizeTime = config.getBoolean("synchronize_time", synchronizeTime);
        synchronizedTimeOffset = config.getLong("time_offset", synchronizedTimeOffset);
        resourcePack = config.getString("resource_pack", resourcePack);
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
        if (config.contains("game_mode")) {
            String modeString = config.getString("game_mode");
            try {
                gameMode = GameMode.valueOf(modeString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid game mode: " + modeString);
            }
        }
        if (config.contains("leave_game_mode")) {
            String modeString = config.getString("leave_game_mode");
            try {
                leavingGameMode = GameMode.valueOf(modeString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid game mode: " + modeString);
            }
        }
        seed = config.getLong("seed", this.seed);
        autoLoad = config.getBoolean("autoload", autoLoad);
        chunkHandler.load(worldName, config.getConfigurationSection("chunk_generate"));
        blockBreakHandler.load(worldName, "break", config.getConfigurationSection("block_break"));
        blockPlaceHandler.load(worldName, "place", config.getConfigurationSection("block_place"));
        spawnHandler.load(worldName, config.getConfigurationSection("entity_spawn"));
        cancelSpellsOnSave = config.getBoolean("cancel_spells_on_save", cancelSpellsOnSave);
    }

    public void finalizeLoad() {
        // Autoload worlds
        if (autoLoad) {
            // Wait a few ticks to do this, to avoid errors during initialization
            Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new CheckWorldCreateTask(this), 1L);
        }
        if (!installed) {
            installPopulators(Bukkit.getWorld(worldName));
        }
        spawnHandler.finalizeLoad();
    }

    public World checkWorldCreate() {
        // Loaded check
        if (state != WorldState.UNLOADED) {
            return Bukkit.getWorld(worldName);
        }

        if (copyFrom.isEmpty()) {
            return createWorld();
        } else {
            World targetWorld = controller.getPlugin().getServer().getWorld(copyFrom);
            if (targetWorld != null) {
                return copyWorld(targetWorld);
            }
        }
        return null;
    }

    public World createWorld() {
        state = WorldState.LOADING;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            controller.info("Loading " + worldName + " as " + worldEnvironment + " (" + worldType + ")");
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
            CompatibilityLib.getCompatibilityUtils().setEnvironment(world, appearanceEnvironment);
            controller.info("Changed " + worldName + " appearance to " + appearanceEnvironment);
        }
        return world;
    }

    public void installPopulators(World world) {
        if (world == null || installed || chunkHandler.isEmpty()) return;
        controller.info("Installing Populators in " + world.getName());
        world.getPopulators().addAll(chunkHandler.getPopulators());
        installed = true;
    }

    public void remove() {
        if (!installed) return;
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.getPopulators().removeAll(chunkHandler.getPopulators());
        }
        installed = false;
    }

    public boolean processEntitySpawn(Plugin plugin, LivingEntity entity) {
        return spawnHandler.process(plugin, entity);
    }

    public BlockResult processBlockBreak(Block block, Player player) {
        return blockBreakHandler.handleBlock(block, player);
    }

    public BlockResult processBlockPlace(Block block, Player player) {
        return blockPlaceHandler.handleBlock(block, player);
    }

    public void onWorldInit(final World initWorld) {
        // Flag loaded worlds
        if (initWorld.getName().equals(worldName)) {
            state = WorldState.LOADED;
            updateTime();
            return;
        }

        // Loaded check
        if (state != WorldState.UNLOADED) {
            return;
        }

        // See if this is a world we want to make a copy of
        // Delay this one tick to avoid issues at startup
        if (autoLoad && !copyFrom.isEmpty() && initWorld.getName().equals(copyFrom)) {
            Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new CopyWorldTask(this, initWorld), 1L);
        }
    }

    public World copyWorld(World targetWorld) {
        if (!copyFrom.isEmpty() && !targetWorld.getName().equals(copyFrom)) {
            controller.getLogger().warning("World " + worldName + " getting created as a copy of " + targetWorld.getName() + ", but is configured to copy " + copyFrom);
        }

        // Create this world if it doesn't exist
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            state = WorldState.LOADING;
            controller.info("Loading " + worldName + " using settings copied from " + targetWorld.getName());
            world = Bukkit.createWorld(new WorldCreator(worldName).copy(targetWorld));
            if (world == null) {
                controller.getLogger().warning("Failed to create world: " + worldName);
            } else if (appearanceEnvironment != null) {
                CompatibilityLib.getCompatibilityUtils().setEnvironment(world, appearanceEnvironment);
                controller.info("Changed " + worldName + " appearance to " + appearanceEnvironment);
            }
        }
        return world;
    }

    public void playerJoined(Mage mage) {
        playerEntered(mage, null, true);
    }

    public void playerEntered(Mage mage, MagicWorld previousWorld) {
        playerEntered(mage, previousWorld, false);
    }

    public void playerEntered(Mage mage, MagicWorld previousWorld, boolean isJoin) {
        joinedDefault(mage, resourcePack, previousWorld, isJoin);
        if (gameMode != null && (previousWorld == null || previousWorld.gameMode != gameMode)) {
            mage.getPlayer().setGameMode(gameMode);
        }
    }

    public void playerLeft(Mage mage, MagicWorld nextWorld) {
        if (leavingGameMode != null && (nextWorld == null || nextWorld.gameMode == null)) {
            mage.getPlayer().setGameMode(leavingGameMode);
        }
    }

    public static void joinedDefault(Mage mage) {
        joinedDefault(mage, null, null, true);
    }

    private static void joinedDefault(Mage mage, String resourcePack, MagicWorld previousWorld, boolean isJoin) {
        MagicController controller = mage.getController();
        if (controller.isResourcePackEnabled()) {
            return;
        }
        if (mage.isResourcePackEnabled()) {
            String fromResourcePack = previousWorld == null ? null : previousWorld.resourcePack;
            if (!Objects.equals(fromResourcePack, resourcePack) || isJoin) {
                controller.promptResourcePack(mage.getPlayer(), resourcePack);
            }
        } else if (isJoin && mage.isResourcePackPrompt()) {
            controller.promptNoResourcePack(mage.getPlayer());
        }
    }

    public void updateTime() {
        updateTimeFrom(null, 0);
    }

    public void updateTimeFrom(World changedWorld, long skipAmount) {
        if (!synchronizeTime || copyFrom.isEmpty() || state != WorldState.LOADED || updatingTime) {
            return;
        }
        World world = Bukkit.getWorld(worldName);
        if (changedWorld != null && worldName.equals(changedWorld.getName())) {
            if (world != null) {
                updatingTime = true;
                changedWorld.setTime(world.getTime() - synchronizedTimeOffset + skipAmount);
                updatingTime = false;
            }
            return;
        }
        if (changedWorld == null) {
            changedWorld = Bukkit.getWorld(copyFrom);
        } else if (!changedWorld.getName().equals(copyFrom)) {
            return;
        }
        if (changedWorld == null) {
            return;
        }
        if (world != null) {
            updatingTime = true;
            world.setTime(changedWorld.getTime() + synchronizedTimeOffset + skipAmount);
            updatingTime = false;
        }
    }

    public boolean isCancelSpellsOnSave() {
        return cancelSpellsOnSave;
    }

    public int getMaxHeight(int defaultHeight) {
        return maxHeight != null ? maxHeight : defaultHeight;
    }

    public int getMinHeight(int defaultHeight) {
        return minHeight != null ? minHeight : defaultHeight;
    }
}
