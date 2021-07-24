package com.elmakers.mine.bukkit.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.listener.WorldPlayerListener;
import com.elmakers.mine.bukkit.world.listener.WorldSpawnListener;

public class WorldController implements Listener {
    private final Map<String, MagicWorld> magicWorlds = new HashMap<>();
    private final MagicController controller;
    private final WorldPlayerListener playerListener;
    private final WorldSpawnListener spawnListener;

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

    public int getCount() {
        return magicWorlds.size();
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        for (MagicWorld notifyWorld : magicWorlds.values()) {
            notifyWorld.onWorldInit(world);
        }
        MagicWorld magicWorld = magicWorlds.get(world.getName());
        if (magicWorld == null) return;

        controller.info("Initializing world " + world.getName());
        magicWorld.installPopulators(world);
    }

    public void onPlayerJoin(Mage mage) {
        MagicWorld magicWorld = getWorld(mage.getLocation().getWorld().getName());
        if (magicWorld == null) return;
        magicWorld.playerJoined(mage);
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
}
