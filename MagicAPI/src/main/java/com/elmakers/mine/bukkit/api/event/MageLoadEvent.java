package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;

/**
 * A custom event that fires after a Mage's data is loaded.
 */
public class MageLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Mage mage;
    private final boolean firstTime;

    public MageLoadEvent(Mage mage, boolean firstTime) {
        this.mage = mage;
        this.firstTime = firstTime;
    }

    public Mage getMage() {
        return mage;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
