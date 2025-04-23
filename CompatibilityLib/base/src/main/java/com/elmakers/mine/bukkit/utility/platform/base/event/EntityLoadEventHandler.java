package com.elmakers.mine.bukkit.utility.platform.base.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityLoadEventHandler implements Listener {
    private final MageController controller;

    public EntityLoadEventHandler(MageController controller) {
        this.controller = controller;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntitiesLoadEvent(EntitiesLoadEvent event) {
        controller.onEntitiesLoaded(event.getChunk(), event.getEntities());
    }
}
