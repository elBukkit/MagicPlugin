package com.elmakers.mine.bukkit.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.listener.WorldPlayerListener;
import com.elmakers.mine.bukkit.world.listener.WorldSpawnListener;

public class WorldController implements Listener {
    private final Map<String, MagicWorld> magicWorlds = new HashMap<>();
    private final MagicController controller;
    private final WorldPlayerListener playerListener;
    private final WorldSpawnListener spawnListener;
    private final Map<String, ConfigurationSection> generatorConfigs = new HashMap<>();
    private final Map<String, ConfigurationSection> populatorConfigs = new HashMap<>();
    private boolean removeInvalidEntities = false;
    private Map<String, String> transferWorlds = new HashMap<>();

    public WorldController(MagicController controller) {
        this.controller = controller;
        playerListener = new WorldPlayerListener(this);
        spawnListener = new WorldSpawnListener(this);
    }

    public void registerEvents() {
        Plugin plugin = controller.getPlugin();
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
        pm.registerEvents(playerListener, plugin);
        pm.registerEvents(spawnListener, plugin);
    }

    public void load(ConfigurationSection configuration) {
        spawnListener.load(configuration);
        removeInvalidEntities = configuration.getBoolean("remove_invalid_entities");
        if (removeInvalidEntities) {
            controller.getLogger().info("Will remove out of bounds entities on chunk load");
        }
        transferWorlds.clear();
        ConfigurationSection transfer = configuration.getConfigurationSection("transfer");
        if (transfer != null) {
            for (String key : transfer.getKeys(false)) {
                transferWorlds.put(key, transfer.getString(key));
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            transferPlayer(player);
        }
    }

    public void loadGenerators(ConfigurationSection configs) {
        generatorConfigs.clear();
        for (String key : configs.getKeys(false)) {
            generatorConfigs.put(key, configs.getConfigurationSection(key));
        }
    }

    public void loadPopulators(ConfigurationSection configs) {
        populatorConfigs.clear();
        for (String key : configs.getKeys(false)) {
            populatorConfigs.put(key, configs.getConfigurationSection(key));
        }
    }

    public void loadWorlds(ConfigurationSection configuration) {
        for (MagicWorld magicWorld : magicWorlds.values()) {
            magicWorld.remove();
        }
        magicWorlds.clear();

        // This is to support backwards compatibility with MagicWorlds configuration, where the worlds
        // where grouped into a "worlds." section
        ConfigurationSection legacyConfig = configuration.getConfigurationSection("worlds");
        if (legacyConfig != null) {
            controller.getLogger().info("Your world configuration contains a 'worlds' section, assuming this is a legacy MagicWorlds configuration");
            ConfigurationUtils.addConfigurations(configuration, legacyConfig, false);
            configuration.set("worlds", null);
        }

        Set<String> worldKeys = configuration.getKeys(false);
        for (String worldName : worldKeys) {
            ConfigurationSection worldConfiguration = configuration.getConfigurationSection(worldName);
            if (worldConfiguration == null) {
                controller.getLogger().warning("Was expecting a properties section in world config for key '" + worldName + "', but got: " + configuration.get(worldName));
                continue;
            }
            if (!worldConfiguration.getBoolean("enabled", true)) continue;

            worldName = worldConfiguration.getString("world", worldName);
            // used for base classes
            if (worldName.isEmpty()) continue;

            MagicWorld world = magicWorlds.get(worldName);
            if (world == null) world = new MagicWorld(controller);
            try {
                world.load(worldName, worldConfiguration);
                magicWorlds.put(worldName, world);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Unexpected error setting up customizations for world: " + worldName, ex);
            }
        }

        // Finalize config load
        for (MagicWorld world : magicWorlds.values()) {
            world.finalizeLoad();
        }
    }

    public World createWorld(String worldName) {
        MagicWorld world = magicWorlds.get(worldName);
        if (world != null) {
            return world.checkWorldCreate();
        }
        return Bukkit.createWorld(new WorldCreator(worldName));
    }

    public World copyWorld(String worldName, World copyFrom) {
        MagicWorld world = magicWorlds.get(worldName);
        if (world != null) {
            return world.copyWorld(copyFrom);
        }
        return Bukkit.createWorld(new WorldCreator(worldName).copy(copyFrom));
    }

    public BaseChunkGenerator parseGenerator(MagicWorld world, ConfigurationSection baseConfig) {
        return parseGenerator(world, baseConfig, "generator");
    }

    public BaseChunkGenerator parseGenerator(MagicWorld world, ConfigurationSection baseConfig, String generatorConfigKey) {
        ConfigurationSection generatorConfig = baseConfig.getConfigurationSection(generatorConfigKey);
        if (generatorConfig == null) {
            return createGenerator(world, baseConfig.getString(generatorConfigKey, ""));
        }
        return BaseChunkGenerator.create(world, null, generatorConfig);
    }

    public BaseChunkGenerator createGenerator(MagicWorld world, String generatorKey) {
        if (generatorKey.isEmpty() || generatorKey.equals("none")) return null;
        ConfigurationSection generatorConfig = generatorConfigs.get(generatorKey);
        if (generatorConfig == null) {
            // Auto-generate empty config
            if (populatorConfigs.containsKey(generatorKey)) {
                ConfigurationSection voidConfig = new MemoryConfiguration();
                voidConfig.set("populators", generatorKey);
                generatorConfigs.put(generatorKey, voidConfig);
                return BaseChunkGenerator.create(world, generatorKey, voidConfig);
            }
            controller.getLogger().warning("Invalid chunk generator: " + generatorKey);
            return null;
        }
        return BaseChunkGenerator.create(world, generatorKey, generatorConfig);
    }

    public ConfigurationSection getPopulatorConfig(String populatorKey) {
        return populatorConfigs.get(populatorKey);
    }

    public void reloadGenerator(MagicWorld world, BaseChunkGenerator generator, String generatorKey) {
        ConfigurationSection generatorConfig = generatorConfigs.get(generatorKey);
        if (generatorConfig != null) {
            generator.load(world, generatorConfig);
        }
    }

    public int getCount() {
        return magicWorlds.size();
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        if (removeInvalidEntities) {
            BlockPopulator populator = CompatibilityLib.getCompatibilityUtils().createOutOfBoundsPopulator(controller.getLogger());
            if (populator != null) {
                world.getPopulators().add(populator);
            }
        }
        for (MagicWorld notifyWorld : magicWorlds.values()) {
            notifyWorld.onWorldInit(world);
        }
        MagicWorld magicWorld = magicWorlds.get(world.getName());
        if (magicWorld == null) return;

        controller.info("Initializing world " + world.getName());
        magicWorld.installPopulators(world);
    }

    public void onPlayerJoin(Mage mage) {
        Player player = mage.getPlayer();
        if (player != null) {
            transferPlayer(player);
        }
        MagicWorld magicWorld = getWorld(mage.getLocation().getWorld().getName());
        if (magicWorld == null) {
            MagicWorld.joinedDefault(mage);
            return;
        }
        magicWorld.playerJoined(mage);
    }

    public void transferPlayer(Player player) {
        String worldName = transferWorlds.get(player.getWorld().getName());
        if (worldName == null) return;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            controller.getLogger().warning("Invalid world in transfer configuration: " + worldName);
            return;
        }
        player.teleport(world.getSpawnLocation());
    }

    public List<MaterialAndData> parseBlocks(String worldKey, ConfigurationSection config, String key, String defaultSet) {
        MaterialSet materialSet = controller.getMaterialSetManager().fromConfig(config, key);
        if (materialSet == null) {
            if (defaultSet == null) {
                return Collections.emptyList();
            }
            controller.info("Invalid block set in world " + worldKey + ": " + key + ", defaulting to " + defaultSet);
            materialSet = controller.getMaterialSetManager().getMaterialSet(defaultSet);
        }
        if (materialSet == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(materialSet.getMaterialsWithData());
    }

    public Plugin getPlugin() {
        return controller.getPlugin();
    }

    public MagicController getMagicController() {
        return controller;
    }

    public MagicWorld getWorld(String name) {
        return magicWorlds.get(name);
    }

    public Collection<MagicWorld> getWorlds() {
        return magicWorlds.values();
    }

    public Logger getLogger() {
        return controller.getLogger();
    }

    public boolean isDataLoaded() {
        return controller.isDataLoaded();
    }

    public void setDisableSpawnReplacement(boolean disable) {
        controller.setDisableSpawnReplacement(disable);
    }

    public boolean isDisableSpawnReplacement() {
        return controller.isDisableSpawnReplacement();
    }

    public WorldSpawnListener getSpawnListener() {
        return spawnListener;
    }

    public Set<String> getGeneratorKeys() {
        return generatorConfigs.keySet();
    }

    public Set<String> getPopulatorKeys() {
        return populatorConfigs.keySet();
    }
}
