package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.GeneratorContext;

public class FlatGenerator extends BaseChunkGenerator {
    private List<MaterialAndData> groundBlocks = Collections.emptyList();
    private int yOffset;
    private IntegerRange height;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        groundBlocks = parseBlocks(config, "blocks", "dirts");
        yOffset = config.getInt("y_offset", 0);
        height = IntegerRange.fromOptionalConfig(getLogger(), config, "height");
        return true;
    }

    @Override
    public void generate(GeneratorContext context) {
        final Random random = context.getRandom();
        final BlockData block = RandomUtils.getRandom(groundBlocks, random).createBlockData();
        final int bedrockLevel = world.getBedrockLevel();
        final int groundLevel = world.getGroundLevel();
        final int height = this.height == null ? groundLevel - bedrockLevel : this.height.getRandom(random);
        final int minY = bedrockLevel + yOffset + 1;
        final int maxY = Math.min(context.getMaxHeight() - 1, minY + height);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    context.setBlock(x, y, z, block);
                }
            }
        }
    }
}
