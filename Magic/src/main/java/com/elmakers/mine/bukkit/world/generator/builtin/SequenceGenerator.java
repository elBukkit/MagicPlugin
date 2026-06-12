package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class SequenceGenerator extends BaseChunkGenerator {
    private List<BaseChunkGenerator> generators = new ArrayList<>();

    public void onLoad(ConfigurationSection config) {
        generators.clear();
        WorldController controller = world.getController().getWorlds();
        List<String> generatorIds = ConfigurationUtils.getStringList(config, "generators");
        for (String generatorId : generatorIds) {
            BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
            if (generator != null) {
                generators.add(generator);
            } else {
                world.getLogger().warning("Invalid generator: " + generatorId);
            }
        }
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        for (BaseChunkGenerator generator : generators) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        super.populate(worldInfo, random, chunkX, chunkZ, region);
        for (BaseChunkGenerator generator : generators) {
            generator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }
}
