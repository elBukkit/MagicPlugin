package com.elmakers.mine.bukkit.world.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;

public class WorldPlayerListener implements Listener {
    private final WorldController controller;

    public WorldPlayerListener(final WorldController controller)
    {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        MagicWorld magicWorld = controller.getWorld(player.getWorld().getName());
        if (magicWorld == null) return;

        magicWorld.playerEntered(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MagicWorld magicWorld = controller.getWorld(player.getWorld().getName());
        if (magicWorld == null) return;

        magicWorld.playerEntered(player);
    }
}
