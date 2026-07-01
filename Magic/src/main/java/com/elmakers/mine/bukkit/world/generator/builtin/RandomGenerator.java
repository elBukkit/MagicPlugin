package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.DistanceWeightedValue;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.OptionalGenerator;

public class RandomGenerator extends OptionalGenerator {
    private List<DistanceWeightedValue<BaseChunkGenerator>> generators = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        generators.clear();
        WorldController controller = world.getController().getWorlds();
        ConfigurationSection generatorsConfig = config.getConfigurationSection("generators");
        if (generatorsConfig != null) {
            for (String generatorId : generatorsConfig.getKeys(false)) {
                if (generatorsConfig.isConfigurationSection(generatorId)) {
                    ConfigurationSection generatorConfig = generatorsConfig.getConfigurationSection(generatorId);
                    BaseChunkGenerator generator = controller.parseGenerator(world, generatorConfig);
                    DistanceWeightedValue<BaseChunkGenerator> entry = DistanceWeightedValue.fromConfig(world.getLogger(), generator, generatorConfig);
                    generators.add(entry);
                } else {
                    BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                    DistanceWeightedValue<BaseChunkGenerator> entry = DistanceWeightedValue.fromString(world.getLogger(), generator, generatorsConfig.getString(generatorId));
                    generators.add(entry);
                }
            }
        } else {
            List<String> generatorIds = config.getStringList("generators");
            if (generatorIds != null) {
                for (String generatorId : generatorIds) {
                    BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                    DistanceWeightedValue<BaseChunkGenerator> entry = DistanceWeightedValue.fromString(world.getLogger(), generator, "1");
                    generators.add(entry);
                }
            } else {
                world.getController().getLogger().warning("Random generator missing 'generators' section");
            }
        }
        return !generators.isEmpty();
    }

    @Override
    protected BaseChunkGenerator getGenerator(long worldSeed, int chunkX, int chunkZ) {
        return RandomUtils.getDistanceWeighted(generators, worldSeed, chunkX, chunkZ);
    }
}
