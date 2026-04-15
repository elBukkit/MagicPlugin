package com.elmakers.mine.bukkit.utility.platform.base.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class PlayerPickupListener implements Listener {
    private MageController controller;

    public PlayerPickupListener(MageController controller) {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (controller.onEntityPickupItem(event.getPlayer(), event.getItem())) {
            event.setCancelled(true);
        }
    }
}
