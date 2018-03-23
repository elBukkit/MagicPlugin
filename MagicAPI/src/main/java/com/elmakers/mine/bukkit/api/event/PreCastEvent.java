package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;

/**
 * A custom event that the Magic plugin will fire any time a
 * Mage casts a Spell.
 */
public class PreCastEvent extends Event implements Cancellable {
    private boolean cancelled;

    private final Mage mage;
    private final Spell spell;

    private static final HandlerList handlers = new HandlerList();

    public PreCastEvent(Mage mage, Spell spell) {
        this.mage = mage;
        this.spell = spell;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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

    public Spell getSpell() {
        return spell;
    }
}
