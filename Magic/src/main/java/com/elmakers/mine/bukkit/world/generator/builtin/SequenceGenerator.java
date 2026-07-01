package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.CompoundGenerator;

public class SequenceGenerator extends CompoundGenerator {
    private List<BaseChunkGenerator> generators = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
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
        return !generators.isEmpty();
    }

    @Override
    protected List<BaseChunkGenerator> getGenerators(long worldSeed, int chunkX, int chunkZ) {
        return generators;
    }
}
