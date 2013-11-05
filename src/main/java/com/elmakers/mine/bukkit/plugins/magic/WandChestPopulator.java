package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.generator.BlockPopulator;

public class WandChestPopulator extends BlockPopulator {

	private final static Logger log = Logger.getLogger("Minecraft");
	private final Spells spells; 
	
	public WandChestPopulator(Spells spells) {
		this.spells = spells;
	}
	
	@Override
	public void populate(World world, Random random, Chunk source) {
		for (int x = 0; x < 15; x++) {
			for (int z = 0; z < 15; z++) {
				for (int y = 0; y < 255; y++) {
					Block block = source.getBlock(x, y, z);
					if (block.getType() == Material.CHEST) {
						Chest chest = (Chest)block.getState();
						Wand wand = Wand.createWand(spells, "explorer");
						chest.getInventory().addItem(wand.getItem());
						String wandNames = "explorer";
						log.info("Added wands to chest: " + wandNames + " at " + (x + source.getX() * 16) + "," + y + "," + (z + source.getZ() * 16));
					}
				}
			}
		}
	}

}
