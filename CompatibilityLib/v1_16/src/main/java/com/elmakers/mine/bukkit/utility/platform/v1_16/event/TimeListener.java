package com.elmakers.mine.bukkit.utility.platform.v1_16.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class TimeListener implements Listener {
    private final MageController controller;

    public TimeListener(final MageController controller) {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        controller.timeSkipped(event.getWorld(), event.getSkipAmount());
    }
}
