package com.elmakers.mine.bukkit.utility;

import java.util.Objects;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;

public class LogMessage {
    @Nullable
    private final String context;
    private final String message;

    public LogMessage(String context, String message) {
        this.context = context;
        this.message = message;
    }

    public String getMessage() {
        if (context == null) {
            return message;
        }
        return ChatColor.AQUA + context + ChatColor.GRAY + ": " + ChatColor.WHITE + message;
    }

    @Override
    public int hashCode() {
        int contextHash = context == null ? 0 : context.hashCode();
        int messageHash = message == null ? 0 : message.hashCode();
        return (contextHash & 0xFFFFFF) | ((messageHash & 0xFFFFFF) << 16);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LogMessage)) return false;
        LogMessage other = (LogMessage)o;
        return Objects.equals(context, other.context) && Objects.equals(message, other.message);
    }
}
