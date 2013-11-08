package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

public class VoidBlockPopulator extends BlockPopulator {

	public VoidBlockPopulator() {
	}
	
	protected void clearChunk(Chunk chunk) {

		for (int x = 0; x < 15; x++) {
			for (int z = 0; z < 15; z++) {
				for (int y = 0; y < 255; y++) {
					Block block = chunk.getBlock(x, y, z);
					block.setType(Material.AIR);
				}
			}
		}
	}
	
	@Override
	public void populate(World world, Random random, Chunk source) {
		if (source.getX() < -32 || source.getX() > 32 || source.getZ() < -32 || source.getZ() > 32) {
			clearChunk(source);
		}
	}
}
