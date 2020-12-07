package com.elmakers.mine.bukkit.magic.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.bgsoftware.wildstacker.api.events.EntityStackEvent;

public class WildStackerListener implements Listener {
    @EventHandler
    public void onEntityStack(EntityStackEvent event) {
        if (event.getTarget().getSpawnCause().toSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }
}
