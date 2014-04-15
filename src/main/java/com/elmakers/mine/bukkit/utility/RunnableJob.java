package com.elmakers.mine.bukkit.utility;

import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class RunnableJob extends BukkitRunnable {
	protected boolean finished = false;
	protected final Logger logger;
	
	protected RunnableJob(Logger logger) {
		this.logger = logger;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public void finish() {
		if (!finished) {
			logger.info("Job Finished");
		}
		finished = true;
		cancel();
	}
}
