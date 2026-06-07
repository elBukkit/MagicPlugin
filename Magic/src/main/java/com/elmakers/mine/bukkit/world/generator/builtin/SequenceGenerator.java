package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;

public class SequenceGenerator extends MagicChunkGenerator {
    private List<MagicChunkGenerator> generators = new ArrayList<>();

    public void load(MagicWorld world, ConfigurationSection config) {
        super.load(world, config);
        generators.clear();
        WorldController controller = world.getController().getWorlds();
        List<String> generatorIds = ConfigurationUtils.getStringList(config, "generators");
        for (String generatorId : generatorIds) {
            MagicChunkGenerator generator = controller.createGenerator(world, generatorId);
            if (generator != null) {
                generators.add(generator);
            } else {
                world.getLogger().warning("Invalid generator: " + generatorId);
            }
        }
    }

    private MagicChunkGenerator getPrimaryGenerator() {
        return generators.get(0);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        for (MagicChunkGenerator generator : generators) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }
}
