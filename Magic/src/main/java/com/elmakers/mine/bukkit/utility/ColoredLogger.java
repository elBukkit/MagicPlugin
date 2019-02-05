package com.elmakers.mine.bukkit.utility;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ColoredLogger extends Logger {
    private final Logger delegate;
    private boolean colorize = true;

    public ColoredLogger(Logger delegate) {
        super(delegate.getName(), delegate.getResourceBundleName());
        this.delegate = delegate;
    }

    @Override
    public void log(LogRecord record) {
        if (colorize) {
            if (record.getLevel().equals(Level.SEVERE)) {
                record.setMessage("\u001b[31m " + record.getMessage() + "\u001b[0m");
            } else if (record.getLevel().equals(Level.WARNING)) {
                record.setMessage("\u001b[33m " + record.getMessage() + "\u001b[0m");
            }
        }
        delegate.log(record);
    }

    public void setColorize(boolean colorize) {
        this.colorize = colorize;
    }
}
