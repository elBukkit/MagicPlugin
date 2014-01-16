package com.elmakers.mine.bukkit.plugins.magic.populator;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.NMSUtils;

public class WandChestRunnable extends BukkitRunnable {
	private World world;
	private int dx = 1;
	private int dz = 0;
	private int segmentLength = 1;
	private int x;
	private int z;
	private int segmentPassed = 0;
	private int chunksProcessed = 0;
	
	final static int messageInterval = 100;
	
	WandChestPopulator populator;
	Random random;
	Logger logger;
	boolean finished = false;
	
	public WandChestRunnable(MagicController spells, World world, int maxy) {
		this.world = world;
		this.random = new Random();
		logger = spells.getPlugin().getLogger();
		if (maxy > 0) {
			populator = spells.getWandChestPopulator();
			populator.setMaxY(maxy);
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public void run() {
		Chunk chunk = world.getChunkAt(x, z);
		if (!NMSUtils.isDone(chunk) || !chunk.isLoaded()) {
			if (!NMSUtils.isDone(chunk) || !chunk.load(false)) {
				logger.info("Done populating chests, found ungenerated chunk");
				finished = true;
				this.cancel();
			}
		} else {
			if ((chunksProcessed % messageInterval) == 0) {
				if (populator != null) {
					logger.info("Looking for chests, processed " + chunksProcessed + " chunks");
				} else {
					logger.info("Looking for wands, searched " + chunksProcessed + " chunks");
				}
			}
			chunksProcessed++;
			
			if (populator != null) {
				populator.populate(world, random, chunk);
			}
			x += dx;
			z += dz;
			segmentPassed++;
			if (segmentPassed == segmentLength) {
				segmentPassed = 0;
				int odx = dx;
				dx = -dz;
				dz = odx;
				if (dz == 0) {
					segmentLength++;
				}
			}
		}
	}
}
