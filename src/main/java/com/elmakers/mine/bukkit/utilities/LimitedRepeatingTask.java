package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class LimitedRepeatingTask implements Runnable {

	protected int iterations;
	protected final int taskId;
	
	public LimitedRepeatingTask(Plugin plugin, int delay, int period, int iterations) {
		this.iterations = iterations;
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
	}
	
	// Override this, but be sure to call super.run
	public void run() {
		this.iterations--;
		if (this.iterations <= 0) {
			cancel();
		}
	}
	
	public void cancel() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
