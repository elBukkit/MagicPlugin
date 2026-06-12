package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class CropsGenerator extends BaseChunkGenerator {
    private boolean consistent = false;
    private boolean searchUp = true;
    private double minAge = 0;
    private double maxAge = 1;
    private int minPosition = 0;
    private int maxPosition = 14;
    private int minWidth = 1;
    private int maxWidth = 3;

    private List<MaterialAndData> foodBlocks = Collections.emptyList();
    private List<MaterialAndData> borderBlocks = Collections.emptyList();
    private List<MaterialAndData> soilBlocks = Collections.emptyList();
    private List<MaterialAndData> supportBlocks = Collections.emptyList();

    @Override
    public void onLoad(ConfigurationSection config) {
        consistent = config.getBoolean("consistent", consistent);
        minAge = config.getDouble("min_age", minAge);
        maxAge = config.getDouble("max_age", maxAge);
        foodBlocks = parseBlocks(config, "crops", "all_crops");
        soilBlocks = parseBlocks(config, "soil", "farmland");
        borderBlocks = parseBlocks(config, "border");
        supportBlocks = parseBlocks(config, "support");
        minPosition = config.getInt("min_x", minPosition);
        maxPosition = config.getInt("max_x", maxPosition);
        minWidth = config.getInt("min_width", minWidth);
        maxWidth = config.getInt("max_width", maxWidth);
        searchUp = config.getBoolean("search_up", searchUp);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final int groundLevel = world.getGroundLevel();
        final MaterialAndData borderBlock = RandomUtils.getRandom(borderBlocks, random);
        final MaterialAndData supportBlock = RandomUtils.getRandom(supportBlocks, random);
        final MaterialAndData soilBlock = RandomUtils.getRandom(soilBlocks, random);
        final int width = RandomUtils.range(random, minWidth, maxWidth);
        final boolean hasBorder = borderBlock != null;
        final int borderWidth = hasBorder ? 2 : 0;
        final int startX = Math.min(16 - width - borderWidth, RandomUtils.range(random, minPosition, maxPosition));
        final int startZ = Math.min(16 - width - borderWidth, RandomUtils.range(random, minPosition, maxPosition));
        final int endX = startX + width + borderWidth;
        final int endZ = startZ + width + borderWidth;

        BlockData cropData = null;

        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                int groundY = searchUp ? getTopBlock(chunk, x, groundLevel, z) : groundLevel;
                final boolean isBorder = x == startX || x == endX - 1 || z == startZ || z == endZ - 1;
                if (hasBorder && isBorder) {
                    chunk.setBlock(x, groundY + 1, z, borderBlock.createBlockData());
                    if (supportBlock != null) {
                        chunk.setBlock(x, groundY, z, supportBlock.createBlockData());
                    }
                } else {
                    if (cropData == null) {
                        MaterialAndData cropMaterial = RandomUtils.getRandom(foodBlocks, random);
                        cropData = cropMaterial.createBlockData();
                        if (cropData instanceof Ageable) {
                            Ageable crops = (Ageable)cropData;
                            double ageRatio = RandomUtils.lerp(minAge, maxAge, random.nextDouble());
                            int age = RandomUtils.lerp(0, crops.getMaximumAge(), ageRatio);
                            crops.setAge(age);
                        }
                    }
                    chunk.setBlock(x, groundY + 1, z, cropData);
                    if (soilBlock != null) {
                        chunk.setBlock(x, groundY, z, soilBlock.createBlockData());
                    }
                    if (!consistent) {
                        cropData = null;
                    }
                }
            }
        }
    }
}
