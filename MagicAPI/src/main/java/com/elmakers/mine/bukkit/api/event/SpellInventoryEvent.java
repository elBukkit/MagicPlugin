package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;

/**
 * This event is fired whenever a player open or closes their spell inventory.
 * This is most often when using a wand. When a spell inventory is open, the player's normal survival inventory
 * is stored, and can be retrieved via Mage.getInventory()
 */
public class SpellInventoryEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final boolean isOpening;
    private final Mage mage;

    public SpellInventoryEvent(Mage mage, boolean isOpening) {
        this.mage = mage;
        this.isOpening = isOpening;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Mage getMage() {
        return mage;
    }

    public boolean isOpening() {
        return this.isOpening;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
