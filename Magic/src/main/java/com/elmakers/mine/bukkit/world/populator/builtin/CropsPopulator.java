package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.DoubleRange;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class CropsPopulator extends BaseBlockPopulator {
    private boolean consistent = false;
    private DoubleRange age;
    private IntegerRange position;
    private IntegerRange size;
    private SearchType searchType = SearchType.CONFORM_UP;

    private List<MaterialAndData> foodBlocks = Collections.emptyList();
    private List<MaterialAndData> borderBlocks = Collections.emptyList();
    private List<MaterialAndData> soilBlocks = Collections.emptyList();
    private List<MaterialAndData> supportBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        consistent = config.getBoolean("consistent", consistent);
        age = DoubleRange.fromConfig(getLogger(), config, "age", 0, 1);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 14);
        size = IntegerRange.fromConfig(getLogger(), config, "size", 1, 3);
        foodBlocks = parseBlocks(config, "crops", "all_crops");
        soilBlocks = parseBlocks(config, "soil", "farmland");
        borderBlocks = parseBlocks(config, "border");
        supportBlocks = parseBlocks(config, "support");
        searchType = ConfigUtils.parseEnum(config, "search", SearchType.CONFORM_UP, getLogger());
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int groundLevel = world.getGroundLevel();
        final MaterialAndData borderBlock = RandomUtils.getRandom(borderBlocks, random);
        final MaterialAndData supportBlock = RandomUtils.getRandom(supportBlocks, random);
        final MaterialAndData soilBlock = RandomUtils.getRandom(soilBlocks, random);
        final boolean hasBorder = borderBlock != null;
        final int borderWidth = hasBorder ? 2 : 0;
        final int width = this.size.getRandom(random);
        context = context.fit(width + borderWidth, width + borderWidth);
        final BoundingBox bounds = context.getBounds();
        final int startX = Math.min(bounds.getMax().getBlockX() - width - borderWidth, position.getRandom(random));
        final int startZ = Math.min(bounds.getMax().getBlockZ() - width - borderWidth, position.getRandom(random));
        final int endX = startX + width + borderWidth;
        final int endZ = startZ + width + borderWidth;
        final int startY = context.searchArea(searchType, startX, groundLevel, startZ, endX, endZ);

        BlockData cropData = null;

        for (int dx = startX; dx < endX; dx++) {
            for (int dz = startZ; dz < endZ; dz++) {
                final int groundY = context.continueSearchArea(searchType, dx, startY, dz, groundLevel);
                final boolean isBorder = dx == startX || dx == endX - 1 || dz == startZ || dz == endZ - 1;
                if (hasBorder && isBorder) {
                    context.setBlockData(dx, groundY + 1, dz, borderBlock.createBlockData());
                    if (supportBlock != null) {
                        context.setBlockData(dx, groundY, dz, supportBlock.createBlockData());
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
                    context.setBlockData(dx, groundY + 1, dz, cropData);
                    if (soilBlock != null) {
                        context.setBlockData(dx, groundY, dz, soilBlock.createBlockData());
                    }
                    if (!consistent) {
                        cropData = null;
                    }
                }
            }
        }
    }
}
