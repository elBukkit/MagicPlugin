package com.elmakers.mine.bukkit.api.event;

import java.util.logging.LogRecord;
import javax.annotation.Nullable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that the Magic plugin calls any time a warning occurs
 */
public class MagicWarningEvent extends Event {
    private final LogRecord logRecord;
    private final String context;
    private final int warningCount;
    private final boolean isCaptured;

    private static final HandlerList handlers = new HandlerList();

    public MagicWarningEvent(LogRecord logRecord, @Nullable String context, int warningCount, boolean isCaptured) {
        this.logRecord = logRecord;
        this.context = context;
        this.warningCount = warningCount;
        this.isCaptured = isCaptured;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public LogRecord getLogRecord() {
        return logRecord;
    }

    public String getContext() {
        return context;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public boolean isCaptured() {
        return isCaptured;
    }
}
