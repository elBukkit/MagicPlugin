package com.elmakers.mine.bukkit.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import com.elmakers.mine.bukkit.magic.MagicController;

public class WorldController implements Listener {
    private final Map<String, MagicWorld> magicWorlds = new HashMap<String, MagicWorld>();
    private final MagicController controller;

    public WorldController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        Set<String> worldKeys = configuration.getKeys(false);
        for (String worldName : worldKeys) {
            controller.info("Customizing world " + worldName);
            MagicWorld world = magicWorlds.get(worldName);
            if (world == null) world = new MagicWorld(controller);
            try {
                world.load(worldName, configuration.getConfigurationSection(worldName));
                magicWorlds.put(worldName, world);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Unexpected error setting up customizations for world: " + worldName, ex);
            }
        }
    }

    public int getCount() {
        return magicWorlds.size();
    }
}
