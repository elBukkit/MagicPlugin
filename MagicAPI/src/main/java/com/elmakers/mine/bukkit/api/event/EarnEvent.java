package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;

/**
 * Called whenever a player earns SP from casting spells.
 * May be called more in the future, so please be aware of the types for now.
 */
public class EarnEvent extends Event implements Cancellable {
    public enum EarnCause {
        SPELL_CAST
    }

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Mage mage;
    private final String earnType;
    private final double earnAmount;
    private final EarnCause earnCause;

    public EarnEvent(Mage mage, String type, double amount, EarnCause cause) {
        this.mage = mage;
        this.earnType = type;
        this.earnAmount = amount;
        this.earnCause = cause;
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

    public String getEarnType() {
        return earnType;
    }

    public double getEarnAmount() {
        return earnAmount;
    }

    public EarnCause getEarnCause() {
        return earnCause;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
