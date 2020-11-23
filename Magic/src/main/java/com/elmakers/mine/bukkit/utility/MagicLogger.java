package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;

public class MagicLogger extends ColoredLogger {
    public static class LogMessage {
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
    }

    private String context = null;
    private boolean capture = false;
    private final List<LogMessage> warnings = new ArrayList<>();
    private final List<LogMessage> errors = new ArrayList<>();

    public MagicLogger(Logger delegate) {
        super(delegate);
    }

    @Override
    public void log(LogRecord record) {
        if (capture) {
            if (record.getLevel().equals(Level.WARNING)) {
                warnings.add(new LogMessage(context, record.getMessage().replace("[Magic] ", "")));
            } else if (record.getLevel().equals(Level.SEVERE)) {
                errors.add(new LogMessage(context, record.getMessage().replace("[Magic] ", "")));
            }
        }
        if (context != null) {
            record.setMessage(context + ": " + record.getMessage());
        }
        super.log(record);
    }

    public void enableCapture(boolean enable) {
        this.capture = enable;
        this.context = null;
        this.warnings.clear();
        this.errors.clear();
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<LogMessage> getErrors() {
        return errors;
    }

    public List<LogMessage> getWarnings() {
        return warnings;
    }

    public boolean isCapturing() {
        return capture;
    }
}
