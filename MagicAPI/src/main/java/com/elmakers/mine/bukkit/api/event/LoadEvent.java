package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that fires whenever Magic loads or reloads configurations.
 */
public class LoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private MageController controller;

    public LoadEvent(MageController controller) {
        this.controller = controller;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public MageController getController() {
        return controller;
    }
}
