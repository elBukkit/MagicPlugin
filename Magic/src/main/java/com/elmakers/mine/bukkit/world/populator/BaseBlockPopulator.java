package com.elmakers.mine.bukkit.world.populator;

import java.util.Random;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.BlockResult;

public abstract class BaseBlockPopulator extends MagicChunkPopulator {
    private int maxY = 255;
    private int minY = 0;
    private int maxAirY = 255;
    private int cooldown;
    private long lastPopulate;
    private Set<Biome> biomes;
    private Set<Biome> notBiomes;

    @Override
    public boolean load(ConfigurationSection config, MagicController controller) {
        if (!super.load(config, controller)) {
            return false;
        }
        maxY = config.getInt("max_y", maxY);
        minY = config.getInt("min_y", minY);
        maxAirY = config.getInt("max_air_y", maxAirY);
        cooldown = config.getInt("cooldown", 0);
        biomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(config, "biomes"), controller.getLogger(), "block populator");
        notBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(config, "not_biomes"), controller.getLogger(), "block populator");
        return true;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = chunk.getBlock(x,  y, z);
                    if (y > maxAirY && block.getType() == Material.AIR) {
                        break;
                    }
                    if (biomes != null && !biomes.contains(block.getBiome()))
                        continue;
                    if (notBiomes != null && notBiomes.contains(block.getBiome()))
                        continue;

                    long now = System.currentTimeMillis();
                    if (cooldown > 0 && now < lastPopulate + cooldown)
                        continue;

                    BlockResult result = populate(block, random);
                    if (result != BlockResult.SKIP) {
                        lastPopulate = now;
                    }
                }
            }
        }
    }

    public abstract BlockResult populate(Block block, Random random);
}
