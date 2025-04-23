package com.elmakers.mine.bukkit.utility.platform.base.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityPickupListener implements Listener {
    private MageController controller;

    public EntityPickupListener(MageController controller) {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (controller.onEntityPickupItem(event.getEntity(), event.getItem())) {
            event.setCancelled(true);
        }
    }
}
