package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class FlatGenerator extends BaseChunkGenerator {
    private List<MaterialAndData> groundBlocks = Collections.emptyList();
    private int yOffset;
    private int thickness;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        groundBlocks = parseBlocks(config, "blocks", "dirts");
        yOffset = config.getInt("y_offset", 0);
        thickness = config.getInt("thickness", 1);
        return true;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final BlockData block = RandomUtils.getRandom(groundBlocks, random).createBlockData();
        final int groundLevel = world.getGroundLevel() + yOffset;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < thickness; y++) {
                    chunk.setBlock(x, groundLevel + y, z, block);
                }
            }
        }
    }
}
