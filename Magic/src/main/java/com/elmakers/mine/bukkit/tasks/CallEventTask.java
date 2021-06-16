package com.elmakers.mine.bukkit.tasks;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class CallEventTask implements Runnable {
    private final Event event;

    public CallEventTask(Event event) {
        this.event = event;
    }

    @Override
    public void run() {
        Bukkit.getPluginManager().callEvent(event);
    }
}
