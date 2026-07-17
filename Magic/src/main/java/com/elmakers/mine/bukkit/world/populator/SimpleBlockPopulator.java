package com.elmakers.mine.bukkit.world.populator;

import java.util.Random;
import java.util.Set;

import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.MagicWorld;

public abstract class SimpleBlockPopulator extends BaseBlockPopulator {
    private Integer maxY = null;
    private Integer minY = null;
    private Integer maxAirY = null;
    private int cooldown;
    private long lastPopulate;
    private Set<Biome> biomes;
    private Set<Biome> notBiomes;

    @Override
    public boolean load(MagicWorld world, ConfigurationSection config) {
        if (!super.load(world, config)) {
            return false;
        }
        maxY = ConfigurationUtils.getOptionalInteger(config, "max_y");
        minY = ConfigurationUtils.getOptionalInteger(config, "min_y");
        maxAirY = ConfigurationUtils.getOptionalInteger(config, "max_air_y");
        cooldown = config.getInt("cooldown", 0);
        biomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(config, "biomes"), getController().getLogger(), "block populator");
        notBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(config, "not_biomes"), getController().getLogger(), "block populator");
        return true;
    }

    @Override
    public void populate(PopulatorContext context) {
        int minY = this.minY != null ? this.minY : context.getMinHeight();
        int maxY = this.maxY != null ? this.maxY : context.getMaxHeight();
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockState block = context.getBlockState(x,  y, z);
                    if (maxAirY != null && y > maxAirY && block.getType().isAir()) {
                        break;
                    }
                    Biome biome = context.getBiome(x, y, z);
                    if (biomes != null && !biomes.contains(biome))
                        continue;
                    if (notBiomes != null && notBiomes.contains(biome))
                        continue;

                    long now = System.currentTimeMillis();
                    if (cooldown > 0 && now < lastPopulate + cooldown)
                        continue;

                    Random random = context.getRandom();
                    BlockResult result = populate(block, random);
                    if (result != BlockResult.SKIP) {
                        lastPopulate = now;
                    }
                }
            }
        }
    }

    public abstract BlockResult populate(BlockState block, Random random);
}
