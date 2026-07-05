package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.DoubleRange;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.GeneratorContext;

public class PerlinGenerator extends BaseChunkGenerator {
    private PerlinNoiseGenerator noise;
    private DoubleRange noiseScale;
    private IntegerRange elevation;
    private IntegerRange clipElevation;

    private List<MaterialAndData> groundBlocks = Collections.emptyList();
    private List<MaterialAndData> topBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        noiseScale = DoubleRange.fromConfig(getLogger(), config, "noise", 0.1, 0.1);
        elevation = IntegerRange.fromConfig(getLogger(), config, "elevation", 0, 0);
        clipElevation = IntegerRange.fromConfig(getLogger(), config, "clip_elevation", 0, 0);
        groundBlocks = parseBlocks(config, "blocks", "dirts");
        topBlocks = parseBlocks(config, "top_blocks");
        return true;
    }

    @Override
    public void generate(GeneratorContext context) {
        synchronized (this) {
            if (noise == null) {
                noise = new PerlinNoiseGenerator(context.getSeed());
            }
        }
        final Random random = context.getRandom();
        final int groundLevel = world.getGroundLevel();
        final int bedrockLevel = world.getBedrockLevel();
        final double noiseScale = this.noiseScale.getRandom(random);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int worldX = context.getWorldX(x);
                final int worldZ = context.getWorldZ(z);
                final double blockValue = (noise.noise(worldX * noiseScale, worldZ * noiseScale) + 1) / 2;
                int elevation = this.elevation.lerp(blockValue);
                elevation = clipElevation.clip(elevation, 0);
                final int blockIndex = RandomUtils.lerp(0, groundBlocks.size() - 1, blockValue);
                final MaterialAndData floorBlock = groundBlocks.get(blockIndex);
                MaterialAndData topBlock = floorBlock;
                if (!topBlocks.isEmpty()) {
                    final int topIndex = RandomUtils.lerp(0, topBlocks.size() - 1, blockValue);
                    topBlock = topBlocks.get(topIndex);
                }
                for (int y = bedrockLevel + 1; y <= groundLevel + elevation; y++) {
                    BlockData blockData = (y == groundLevel + elevation) ? topBlock.createBlockData() : floorBlock.createBlockData();
                    context.setBlock(x, y, z, blockData);
                }
            }
        }
    }
}
