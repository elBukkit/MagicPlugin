package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

// This class can be used as a base class for a repeating task that you
// want to "manage".
// This class will store its own task id so you can cancel it when needed.
public abstract class ManagedRepeatingTask implements Runnable {

	protected final int taskId;
	
	public ManagedRepeatingTask(Plugin plugin, int delay, int period) {
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
	}
	
	public void cancel() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
