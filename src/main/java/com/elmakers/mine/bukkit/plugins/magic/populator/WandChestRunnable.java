package com.elmakers.mine.bukkit.plugins.magic.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.NMSUtils;

public class WandChestRunnable extends MagicRunnable {
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
	
	public WandChestRunnable(MagicController controller, World world, int maxy) {
		super(controller.getLogger());
		this.world = world;
		this.random = new Random();
		if (maxy > 0) {
			populator = controller.getWandChestPopulator();
			populator.setMaxY(maxy);
		}
	}
	
	public void finish() {
		super.finish();
		populator = null;
		world = null;
	}
	
	public void run() {
		Chunk chunk = world.getChunkAt(x, z);
		if (!NMSUtils.isDone(chunk) || !chunk.isLoaded()) {
			if (!NMSUtils.isDone(chunk) || !chunk.load(false)) {
				logger.info("Done populating chests, found ungenerated chunk");
				finish();
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
