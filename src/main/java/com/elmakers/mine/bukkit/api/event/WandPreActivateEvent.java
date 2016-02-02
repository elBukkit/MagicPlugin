package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that the Magic plugin will fire whenever a player equips
 * a wand but before the wand becomes activated.
 */
public class WandPreActivateEvent extends Event implements Cancellable {
    private final Mage mage;
    private final Wand wand;
    private boolean cancelled = false;

    private static final HandlerList handlers = new HandlerList();

    public WandPreActivateEvent(Mage mage, Wand wand) {
        this.mage = mage;
        this.wand = wand;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Mage getMage() {
        return mage;
    }

    public Wand getWand() {
        return wand;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
