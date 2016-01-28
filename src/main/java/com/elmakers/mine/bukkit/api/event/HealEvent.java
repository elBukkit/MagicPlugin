package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired whenever a healing effect from a spell is produced.
 */
public class HealEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final CastContext context;
    private double healedAmount;
    private boolean cancelled;

    public HealEvent(CastContext context, double healingAmount) {
        this.context = context;
        this.healedAmount = healingAmount;
        this.cancelled = false;
    }

    public CastContext getContext() {
        return context;
    }

    public double getHealingAmount() {
        return healedAmount;
    }

    public void setHealingAmount(double healingAmount) {
        this.healedAmount = healingAmount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
