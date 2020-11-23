package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MagicLogger extends ColoredLogger {

    private String context = null;
    private boolean capture = false;
    private final Set<LogMessage> warnings = new LinkedHashSet<>();
    private final Set<LogMessage> errors = new LinkedHashSet<>();

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
        return new ArrayList<>(errors);
    }

    public List<LogMessage> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public boolean isCapturing() {
        return capture;
    }
}
