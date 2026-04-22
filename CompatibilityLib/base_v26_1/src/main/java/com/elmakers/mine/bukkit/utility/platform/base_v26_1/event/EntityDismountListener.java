package com.elmakers.mine.bukkit.utility.platform.base_v26_1.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityDismountListener implements Listener {
    private final MageController controller;

    public EntityDismountListener(MageController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        controller.onEntityDismount(event.getEntity());
    }
}
