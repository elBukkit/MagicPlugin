package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class FlatGenerator extends BaseChunkGenerator {
    private List<MaterialAndData> groundBlocks = Collections.emptyList();
    private int yOffset;
    private Integer thickness;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        groundBlocks = parseBlocks(config, "blocks", "dirts");
        yOffset = config.getInt("y_offset", 0);
        thickness = ConfigUtils.getOptionalInteger(config, "thickness");
        return true;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final BlockData block = RandomUtils.getRandom(groundBlocks, random).createBlockData();
        final int bedrockLevel = world.getBedrockLevel();
        final int groundLevel = world.getGroundLevel();
        final int thickness = this.thickness == null ? groundLevel - bedrockLevel : this.thickness;
        final int maxY = groundLevel + yOffset + 1;
        final int minY = maxY - thickness;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    chunk.setBlock(x, y, z, block);
                }
            }
        }
    }
}
