package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that fires whenever Magic saves.
 */
public class SaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public SaveEvent() {
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
