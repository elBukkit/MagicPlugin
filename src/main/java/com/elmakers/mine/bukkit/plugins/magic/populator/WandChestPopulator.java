package com.elmakers.mine.bukkit.plugins.magic.populator;

import java.util.List;
import java.util.Random;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.generator.BlockPopulator;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.RandomUtils;
import com.elmakers.mine.bukkit.utilities.WeightedPair;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WandChestPopulator extends BlockPopulator {
	private final Logger log;
	private final MagicController spells; 
	private final LinkedList<WeightedPair<Integer>> baseProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<String>> wandProbability = new LinkedList<WeightedPair<String>>();
	private int maxy = 255;
	
	public WandChestPopulator(MagicController spells, ConfigurationNode config) {
		this.spells = spells;
		this.log = spells.getLog();
		
		// Fetch base probabilities
		Float currentThreshold = 0.0f;
		ConfigurationNode base = config.getNode("base_probability");
		if (base != null) {
			List<String> keys = base.getKeys();
			for (String key : keys) {
				Integer wandCount = Integer.parseInt(key);
				Float threshold = (float)base.getDouble(key, 0);
				currentThreshold += threshold;
				baseProbability.add(new WeightedPair<Integer>(currentThreshold, wandCount));
			}
		}
		
		// Fetch wand probabilities
		currentThreshold = 0.0f;
		ConfigurationNode wands = config.getNode("wand_probability");
		if (wands != null) {
			List<String> keys = wands.getKeys();
			for (String key : keys) {
				String wandName = key;
				Float threshold = (float)wands.getDouble(key, 0);
				currentThreshold += threshold;
				wandProbability.add(new WeightedPair<String>(currentThreshold, wandName));
			}
		}
	}
	
	protected String[] populateChest(Chest chest) {
		// First determine how many wands to add
		Integer wandCount = RandomUtils.weightedRandom(baseProbability);
		String[] wandNames = new String[wandCount];
		for (int i = 0; i < wandCount; i++) {
			String wandName = RandomUtils.weightedRandom(wandProbability);
			Wand wand = Wand.createWand(spells, wandName);
			if (wand != null) {
				chest.getInventory().addItem(wand.getItem());
			} else {
				wandName = "*" + wandName;
			}
			wandNames[i] = wandName;
		}
		
		return wandNames;
	}
	
	public void setMaxY(int maxy) {
		this.maxy = maxy;
	}
	
	@Override
	public void populate(World world, Random random, Chunk source) {
		for (int x = 0; x < 15; x++) {
			for (int z = 0; z < 15; z++) {
				for (int y = 0; y < maxy; y++) {
					Block block = source.getBlock(x, y, z);
					if (block.getType() == Material.CHEST) {
						Chest chest = (Chest)block.getState();
						String[] wandNames = populateChest(chest);
						if (wandNames.length > 0 && log != null) {
							log.info("Added wands to chest: " + StringUtils.join(wandNames, ", ") + " at " + (x + source.getX() * 16) + "," + y + "," + (z + source.getZ() * 16));
						}
					}	
				}
			}
		}
	}

}
