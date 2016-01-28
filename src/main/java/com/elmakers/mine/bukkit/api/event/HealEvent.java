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
    private double amount;
    private double percent;
    private boolean cancelled;

    public HealEvent(CastContext context, double amount, double percent) {
        this.context = context;
        this.amount = amount;
        this.percent = percent;
        this.cancelled = false;
    }

    public CastContext getContext() {
        return context;
    }

    /**
     * The amount that will be healed unless {@link #getHealPercent()} is greater than 0.
     */
    public double getHealAmount() {
        return amount;
    }

    /**
     * The amount that will be healed unless {@link #getHealPercent()} is greater than 0.
     */
    public void setHealAmount(double healAmount) {
        this.amount = healAmount;
    }

    /**
     * If the heal percent is greater than 0 then the healing will occur as a percent of the targets total health
     * instead of the flat amount indicated by {@link #getHealAmount()}.
     */
    public double getHealPercent() {
        return percent;
    }

    /**
     * If the heal percent is greater than 0 then the healing will occur as a percent of the targets total health
     * instead of the flat amount indicated by {@link #getHealAmount()}.
     */
    public void setHealPercent(double healPercent) {
        this.percent = healPercent;
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
