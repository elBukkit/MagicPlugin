package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.events.ArenaPlayerJoinEvent;
import com.garbagemule.MobArena.events.ArenaPlayerLeaveEvent;
import com.garbagemule.MobArena.things.Thing;
import com.garbagemule.MobArena.things.ThingParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class MobArenaManager implements Listener, ThingParser {
    private final MageController controller;

    public MobArenaManager(MageController controller, Plugin mobArenaPlugin) {
        this.controller = controller;

        Set<String> magicMobKeys = controller.getMobKeys();
        for (String mob : magicMobKeys) {
            // Have to obey special MobArena naming restrictions
            String mobKey = mob.toLowerCase().replaceAll("[-_\\.]", "");
            new MagicMACreature(controller, mobKey, controller.getMob(mob));
        }

        ((MobArena)mobArenaPlugin).getThingManager().register(this);
        Bukkit.getPluginManager().registerEvents(this, controller.getPlugin());
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

    @Override
    public Thing parse(String s) {
        if (!s.startsWith("magic:")) return null;

        org.bukkit.Bukkit.getConsoleSender().sendMessage(org.bukkit.ChatColor.RED + "  PARSING: " + s);

        s = s.substring(6);
        return new MobArenaItemStackThing(s, controller.createItem(s));
    }
}
