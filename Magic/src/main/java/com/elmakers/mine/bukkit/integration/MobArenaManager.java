package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.protection.BlockBuildManager;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.events.ArenaPlayerJoinEvent;
import com.garbagemule.MobArena.events.ArenaPlayerLeaveEvent;
import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.framework.ArenaMaster;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MobArenaManager implements Listener, BlockBreakManager, BlockBuildManager {
    private final MageController controller;
    private boolean protect;
    private List<String> protectedArenas = null;
    private MobArena mobArena;

    public MobArenaManager(MageController controller, Plugin plugin, ConfigurationSection configuration) {
        this.controller = controller;

        Set<String> magicMobKeys = controller.getMobKeys();
        for (String mob : magicMobKeys) {
            // Have to obey special MobArena naming restrictions
            String mobKey = mob.toLowerCase().replaceAll("[-_\\.]", "");
            new MagicMACreature(controller, mobKey, controller.getMob(mob));
        }
        
        if (plugin instanceof MobArena) {
            mobArena = (MobArena)plugin;
        }
        configure(configuration);

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

    // Hopefully can use this again one day for custom item provider
    @Nullable public ItemStack getItem(String s) {
        if (!s.startsWith("magic:")) return null;
        s = s.substring(6);
        return controller.createItem(s);
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
