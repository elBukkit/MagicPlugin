package com.elmakers.mine.bukkit.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

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
            worldName = worldConfiguration.getString("world", worldName);
            controller.info("Customizing world " + worldName);
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

    public Plugin getPlugin() {
        return controller.getPlugin();
    }

    public MagicWorld getWorld(String name) {
        return magicWorlds.get(name);
    }

    public Logger getLogger() {
        return controller.getLogger();
    }
}
