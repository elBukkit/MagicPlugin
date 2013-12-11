package com.elmakers.mine.bukkit.utilities;

import org.bukkit.plugin.Plugin;

// This can be used as a base class for a repeating task that you want
// to end after a certain number of iterations (repeats).
public abstract class LimitedRepeatingTask extends ManagedRepeatingTask {

	protected int iterations;
	
	public LimitedRepeatingTask(Plugin plugin, int delay, int period, int iterations) {
		super(plugin, delay, period);
		this.iterations = iterations;
	}
	
	// Don't override this, override onRepeat()
	public void run() {
		onRepeat();
		this.iterations--;
		if (this.iterations <= 0) {
			cancel();
		}
	}
	
	public abstract void onRepeat();
}
