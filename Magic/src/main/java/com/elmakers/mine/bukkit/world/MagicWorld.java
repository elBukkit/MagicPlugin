package com.elmakers.mine.bukkit.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.block.MagicBlockHandler;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;
import com.elmakers.mine.bukkit.world.populator.MagicChunkHandler;
import com.elmakers.mine.bukkit.world.populator.MagicChunkPopulator;
import com.elmakers.mine.bukkit.world.spawn.MagicSpawnHandler;
import com.elmakers.mine.bukkit.world.tasks.CheckWorldCreateTask;
import com.elmakers.mine.bukkit.world.tasks.CopyWorldTask;

import de.slikey.effectlib.util.CustomSound;

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
    private MagicChunkGenerator generator;
    private int groundLevel = 64;
    private int bedrockLevel = 0;
    private Integer time;
    private Integer titleDelay;
    private Vector spawnPosition;
    private Map<String, Boolean> gameRules = new HashMap<>();
    private String respawnWorld;
    private CustomSound[] ambientSounds = {};
    private int minAmbientSoundTime;
    private int maxAmbientSoundTime;

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
        groundLevel = config.getInt("ground_level", groundLevel);
        bedrockLevel = config.getInt("bedrock_level", bedrockLevel);
        copyFrom = config.getString("copy", copyFrom);
        synchronizeTime = config.getBoolean("synchronize_time", synchronizeTime);
        synchronizedTimeOffset = config.getLong("time_offset", synchronizedTimeOffset);
        resourcePack = config.getString("resource_pack", resourcePack);
        spawnPosition = ConfigurationUtils.getVector(config, "spawn");
        minAmbientSoundTime = config.getInt("min_ambient_sound_time", 0) * 20;
        maxAmbientSoundTime = config.getInt("max_ambient_sound_time", 0) * 20;
        ambientSounds = ConfigurationUtils.getSounds(config, "ambient_sounds");
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
        time = ConfigurationUtils.getOptionalInteger(config, "time");
        titleDelay = ConfigurationUtils.getOptionalInteger(config, "title_delay");
        respawnWorld = config.getString("respawn");
        String generatorKey = config.getString("generator");
        if (generatorKey != null && !generatorKey.isEmpty()) {
            generator = controller.getWorlds().createGenerator(this, generatorKey);
        }
        gameRules.clear();
        ConfigurationSection gameRulesConfig = config.getConfigurationSection("game_rules");
        if (gameRulesConfig != null) {
            for (String rule : gameRulesConfig.getKeys(false)) {
                gameRules.put(rule, gameRulesConfig.getBoolean(rule));
            }
        }

        // Reconfigure existing worlds
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            reconfigureWorld(existingWorld);
        }

        scheduleAmbientSounds();
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
            if (generator != null) {
                worldCreator.generator(generator);
            } else {
                worldCreator.generateStructures(true);
            }
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
        configureWorld(world);
        return world;
    }

    private void reconfigureWorld(World world) {
        if (world == null) return;

        ChunkGenerator chunkGenerator = world.getGenerator();
        if (chunkGenerator instanceof MagicChunkGenerator generator) {
            controller.getWorlds().reloadGenerator(this, generator);
        }

        configureWorld(world);
    }

    private void configureWorld(World world) {
        if (world == null) return;

        if (appearanceEnvironment != null) {
            CompatibilityLib.getCompatibilityUtils().setEnvironment(world, appearanceEnvironment);
            controller.info("Changed " + worldName + " appearance to " + appearanceEnvironment);
        }

        for (Map.Entry<String, Boolean> entry : gameRules.entrySet()) {
            String key = entry.getKey();
            GameRule rule = GameRule.getByName(key);
            if (rule != null) {
                world.setGameRule(rule, entry.getValue());
            } else {
                controller.getLogger().warning("Invalid game rule: " + key);
            }
        }
        if (time != null) {
            world.setTime(time);
        }
    }

    public void installPopulators(World world) {
        if (world == null || installed || chunkHandler.isEmpty()) return;
        Collection<MagicChunkPopulator> populators = chunkHandler.getPopulators();
        if (populators == null || populators.isEmpty()) return;
        controller.info("Installing Populators in " + world.getName());
        world.getPopulators().addAll(populators);
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
        Player player = mage.getPlayer();
        if (titleDelay != null && player != null) {
            Plugin plugin = controller.getPlugin();
            plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> player.sendTitle(
                            ChatColor.translateAlternateColorCodes('&', getTitle()),
                            null,
                            2 * 20,
                            4 * 20,
                            2 * 20
                    ),
                    titleDelay * 20 / 1000);
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
        if (!controller.isResourcePackEnabled()) {
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

    private void scheduleAmbientSounds() {
        if (minAmbientSoundTime == 0 || maxAmbientSoundTime == 0 || ambientSounds.length == 0) {
            return;
        }

        final int soundTime = RandomUtils.range(random, minAmbientSoundTime, maxAmbientSoundTime);
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTaskLater(
            plugin,
            () -> {
                final CustomSound sound = ambientSounds[random.nextInt(ambientSounds.length)];
                final World world = getWorld();
                if (world != null) {
                    for (Player player : world.getPlayers()) {
                        sound.play(plugin, player);
                    }
                    scheduleAmbientSounds();
                }
            },
            soundTime
        );
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
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

    public String getName() {
        return worldName;
    }

    public String getTitle() {
        return controller.getMessages().get("worlds." + worldName + ".name", worldName);
    }

    public Logger getLogger() {
        return controller.getLogger();
    }

    public MagicController getController() {
        return controller;
    }

    public Location getSpawnLocation(World world) {
        if (spawnPosition != null) {
            return new Location(world, spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());
        }
        return null;
    }

    public int getGroundLevel() {
        return groundLevel;
    }

    public int getBedrockLevel() {
        return bedrockLevel;
    }

    public String getRespawnWorld() {
        return respawnWorld;
    }
}
