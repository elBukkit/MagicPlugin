package com.elmakers.mine.bukkit.integration.mobarena;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.events.ArenaPlayerJoinEvent;
import com.garbagemule.MobArena.events.ArenaPlayerLeaveEvent;
import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.framework.ArenaMaster;

public class MobArenaManager implements Listener, BlockBreakManager, BlockBuildManager {
    private final MageController controller;
    private boolean protect;
    private List<String> protectedArenas = null;
    private MobArena mobArena;

    public MobArenaManager(MageController controller, Plugin plugin, ConfigurationSection configuration) {
        this.controller = controller;
        configure(configuration);
        if (plugin instanceof MobArena) {
            mobArena = (MobArena)plugin;
        }
    }

    public void loaded() {
        if (mobArena == null) {
             controller.getLogger().log(Level.WARNING, "Error integrating with MobArena. Could not find main MobArena class");
             return;
        }

        Set<String> magicMobKeys = controller.getMobKeys();
        for (String mob : magicMobKeys) {
            // Have to obey special MobArena naming restrictions
            String mobKey = mob.toLowerCase().replaceAll("[-_\\.]", "");
            new MagicCreature(controller, mobKey, controller.getMob(mob));
        }
        try {
            mobArena.getThingManager().register(new MagicItemStackParser(controller));
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Error integrating with MobArena. You may need to update MobArena for Magic wands and items to work in MobArena configs!", ex);
        }

        Bukkit.getPluginManager().registerEvents(this, controller.getPlugin());
    }

    public void configure(ConfigurationSection configuration) {
        protect = configuration.getBoolean("protect");
        if (protect) {
            controller.getLogger().info("Spells that break or build blocks can't be used in MobArenas");
        }
        if (configuration.contains("protected")) {
            protectedArenas = configuration.getStringList("protected");
        } else {
            protectedArenas = null;
        }
    }

    @EventHandler
    public void onPlayerJoinArena(ArenaPlayerJoinEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player.getUniqueId().toString());
        if (mage != null) {
            mage.deactivate();
        }
    }

    @EventHandler
    public void onPlayerLeaveArena(ArenaPlayerLeaveEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player.getUniqueId().toString());
        if (mage != null) {
            mage.deactivate();
        }
    }

    public boolean isProtected() {
        return mobArena != null && (protect || protectedArenas != null);
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        ArenaMaster am = mobArena.getArenaMaster();
        if (protect) {
            for (Arena arena : am.getArenas()) {
                if (arena.isProtected() && arena.getRegion().contains(block.getLocation())) {
                    return false;
                }
            }
        }
        if (protectedArenas != null) {
            for (String arenaName : protectedArenas) {
                Arena arena = am.getArenaWithName(arenaName);
                if (arena != null && arena.isProtected() && arena.getRegion().contains(block.getLocation())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        return hasBreakPermission(player, block);
    }
}
