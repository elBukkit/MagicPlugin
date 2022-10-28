package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.arena.Arena;

/**
 * A custom event that the Magic plugin will fire any time an arena duel ends.
 */
public class ArenaStopEvent extends Event {
    private final Arena arena;

    private static final HandlerList handlers = new HandlerList();

    public ArenaStopEvent(Arena arena) {
        this.arena = arena;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Arena getArena() {
        return arena;
    }
}
