package com.elmakers.mine.bukkit.world.listener;

import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;

import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;

public class TimeListener implements Listener {
    private final WorldController controller;
    private boolean updating = false;

    public TimeListener(final WorldController controller)
    {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        if (updating) return;

        updating = true;
        World changedWorld = event.getWorld();
        for (MagicWorld world : controller.getWorlds()) {
            try {
                if (!world.updateTimeFrom(changedWorld, event.getSkipAmount())) {
                    event.setCancelled(true);
                    break;
                }
            } catch (Exception ex) {
                controller.getLogger().log(Level.SEVERE, "An error occurred updating world time", ex);
            }
        }
        updating = false;
    }
}
