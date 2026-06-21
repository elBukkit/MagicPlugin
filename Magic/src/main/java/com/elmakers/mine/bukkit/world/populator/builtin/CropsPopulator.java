package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.DoubleRange;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class CropsPopulator extends BaseBlockPopulator {
    private boolean consistent = false;
    private boolean searchUp = true;
    private DoubleRange age;
    private IntegerRange position;
    private IntegerRange width;

    private List<MaterialAndData> foodBlocks = Collections.emptyList();
    private List<MaterialAndData> borderBlocks = Collections.emptyList();
    private List<MaterialAndData> soilBlocks = Collections.emptyList();
    private List<MaterialAndData> supportBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        consistent = config.getBoolean("consistent", consistent);
        age = DoubleRange.fromConfig(getLogger(), config, "age", 0, 1);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 14);
        width = IntegerRange.fromConfig(getLogger(), config, "width", 1, 3);
        foodBlocks = parseBlocks(config, "crops", "all_crops");
        soilBlocks = parseBlocks(config, "soil", "farmland");
        borderBlocks = parseBlocks(config, "border");
        supportBlocks = parseBlocks(config, "support");
        searchUp = config.getBoolean("search_up", searchUp);
        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int groundLevel = world.getGroundLevel();
        final MaterialAndData borderBlock = RandomUtils.getRandom(borderBlocks, random);
        final MaterialAndData supportBlock = RandomUtils.getRandom(supportBlocks, random);
        final MaterialAndData soilBlock = RandomUtils.getRandom(soilBlocks, random);
        final int width = this.width.getRandom(random);
        final boolean hasBorder = borderBlock != null;
        final int borderWidth = hasBorder ? 2 : 0;
        final int startX = Math.min(16 - width - borderWidth, position.getRandom(random));
        final int startZ = Math.min(16 - width - borderWidth, position.getRandom(random));
        final int endX = startX + width + borderWidth;
        final int endZ = startZ + width + borderWidth;
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;

        BlockData cropData = null;

        for (int dx = startX; dx < endX; dx++) {
            for (int dz = startZ; dz < endZ; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;
                int groundY = searchUp ? getTopBlock(worldInfo, region, x, groundLevel, z) : groundLevel;
                final boolean isBorder = x == startX || x == endX - 1 || z == startZ || z == endZ - 1;
                if (hasBorder && isBorder) {
                    region.setBlockData(x, groundY + 1, z, borderBlock.createBlockData());
                    if (supportBlock != null) {
                        region.setBlockData(x, groundY, z, supportBlock.createBlockData());
                    }
                } else {
                    if (cropData == null) {
                        MaterialAndData cropMaterial = RandomUtils.getRandom(foodBlocks, random);
                        cropData = cropMaterial == null ? null : cropMaterial.createBlockData();
                        if (cropData == null) continue;
                        if (cropData instanceof Ageable) {
                            Ageable crops = (Ageable)cropData;
                            double ageRatio = age.getRandom(random);
                            int age = RandomUtils.lerp(0, crops.getMaximumAge(), ageRatio);
                            crops.setAge(age);
                        }
                    }
                    region.setBlockData(x, groundY + 1, z, cropData);
                    if (soilBlock != null) {
                        region.setBlockData(x, groundY, z, soilBlock.createBlockData());
                    }
                    if (!consistent) {
                        cropData = null;
                    }
                }
            }
        }
    }
}
