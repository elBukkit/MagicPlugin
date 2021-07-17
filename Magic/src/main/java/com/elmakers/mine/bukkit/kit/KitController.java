package com.elmakers.mine.bukkit.kit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.configuration.MagicConfiguration;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.tasks.ProcessKitsTask;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class KitController implements Listener {
    private final MagicController controller;
    private final Map<String, MagicKit> kits = new HashMap<>();
    private final List<MagicKit> joinKits = new ArrayList<>();

    public KitController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        kits.clear();
        joinKits.clear();
        Set<String> keys = configuration.getKeys(false);
        for (String key : keys) {
            ConfigurationSection kitConfiguration = configuration.getConfigurationSection(key);
            if (!ConfigurationUtils.isEnabled(kitConfiguration)) continue;
            kitConfiguration = MagicConfiguration.getKeyed(controller, kitConfiguration, "kit", key);
            MagicKit kit = new MagicKit(controller, key, kitConfiguration);
            kits.put(key, kit);
            if (kit.isKeep() || kit.isRemove() || kit.isStarter()) {
                joinKits.add(kit);
            }
        }
        Collections.sort(joinKits);
    }

    public int getCount() {
        return kits.size();
    }

    public MagicKit getKit(String key) {
        return kits.get(key);
    }

    public Set<String> getKitKeys() {
        return kits.keySet();
    }

    public void onJoin(Mage mage) {
        Location location = mage.getLocation();
        World world = location == null ? null : location.getWorld();
        for (MagicKit joinKit : joinKits) {
            if (joinKit.isWorldSpecific() && !joinKit.isWorld(world)) continue;

            if (joinKit.isStarter()) {
                joinKit.checkGive(mage);
            }
            if (joinKit.isRemove()) {
                joinKit.checkRemoveFrom(mage);
            }
            if (joinKit.isKeep()) {
                joinKit.giveMissing(mage);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // We need to delay this one tick so PerWorldInventory or similar plugins
        // can be done messing about with the inventory
        ProcessKitsTask task = new ProcessKitsTask(mage, fromWorld, toWorld);
        for (MagicKit joinKit : joinKits) {
            if (joinKit.isWorld(toWorld)) {
                task.addKit(joinKit);
            }
        }
        if (!task.isEmpty()) {
            Plugin plugin = controller.getPlugin();
            plugin.getServer().getScheduler().runTaskLater(plugin, task, 1);
        }
    }
}
