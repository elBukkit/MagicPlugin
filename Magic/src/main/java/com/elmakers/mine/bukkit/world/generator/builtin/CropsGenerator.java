package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;

public class CropsGenerator extends MagicChunkGenerator {
    private boolean consistent = true;
    private double minAge = 0;
    private double maxAge = 1;

    private List<MaterialAndData> foodBlocks = Collections.emptyList();
    private List<MaterialAndData> borderBlocks = Collections.emptyList();

    @Override
    public void onLoad(ConfigurationSection config) {
        final MageController controller = world.getController();
        consistent = config.getBoolean("consistent", consistent);
        minAge = config.getDouble("min_age", minAge);
        maxAge = config.getDouble("max_age", maxAge);
        MaterialSet foodSet = controller.getMaterialSetManager().fromConfig(config, "crops");
        if (foodSet == null) {
            world.getLogger().warning("Invalid food set: " + config.getString("crops") + ", defaulting to all_crops");
            foodSet = controller.getMaterialSetManager().getMaterialSet("all_crops");
        }
        if (foodSet != null) {
            foodBlocks = new ArrayList<>(foodSet.getMaterialsWithData());
        } else {
            foodBlocks = Collections.emptyList();
        }
        MaterialSet borderSet = controller.getMaterialSetManager().fromConfig(config, "border");
        if (borderSet != null) {
            borderBlocks = new ArrayList<>(borderSet.getMaterialsWithData());
        } else {
            borderBlocks = Collections.emptyList();
        }
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final int groundLevel = world.getGroundLevel();
        MaterialAndData borderBlock = RandomUtils.getRandom(borderBlocks, random);
        BlockData cropData = null;

        for (int x = 6; x < 10; x++) {
            for (int z = 6; z < 10; z++) {
                if (x == 6 || x == 9 || z == 6 || z == 9) {
                    if (borderBlock != null) {
                        chunk.setBlock(x, groundLevel + 1, z, borderBlock.createBlockData());
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
                    chunk.setBlock(x, groundLevel + 1, z, cropData);
                    if (!consistent) {
                        cropData = null;
                    }
                }
            }
        }
    }
}
