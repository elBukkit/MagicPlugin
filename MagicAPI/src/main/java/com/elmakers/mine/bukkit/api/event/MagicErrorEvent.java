package com.elmakers.mine.bukkit.api.event;

import java.util.logging.LogRecord;
import javax.annotation.Nullable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that the Magic plugin calls any time an error occurs
 */
public class MagicErrorEvent extends Event {
    private final LogRecord logRecord;
    private final String context;
    private final int errorCount;
    private final boolean isCaptured;

    private static final HandlerList handlers = new HandlerList();

    public MagicErrorEvent(LogRecord logRecord, @Nullable String context, int errorCount, boolean isCaptured) {
        this.logRecord = logRecord;
        this.context = context;
        this.errorCount = errorCount;
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

    public int getErrorCount() {
        return errorCount;
    }

    public boolean isCaptured() {
        return isCaptured;
    }
}
