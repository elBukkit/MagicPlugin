package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.DistanceWeightedValue;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.OptionalPopulator;

public class RandomPopulator extends OptionalPopulator {
    private List<DistanceWeightedValue<BaseBlockPopulator>> populators = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        populators.clear();
        ConfigurationSection populatorsConfig = config.getConfigurationSection("populators");
        if (populatorsConfig != null) {
            for (String populatorId : populatorsConfig.getKeys(false)) {
                if (populatorsConfig.isConfigurationSection(populatorId)) {
                    ConfigurationSection populatorConfig = populatorsConfig.getConfigurationSection(populatorId);
                    BaseBlockPopulator populator = BaseBlockPopulator.parsePopulator(world, populatorConfig);
                    DistanceWeightedValue<BaseBlockPopulator> entry = DistanceWeightedValue.fromConfig(world.getLogger(), populator, populatorConfig);
                    populators.add(entry);
                } else {
                    BaseBlockPopulator generator = BaseBlockPopulator.loadPopulator(world, populatorId);
                    DistanceWeightedValue<BaseBlockPopulator> entry = DistanceWeightedValue.fromString(world.getLogger(), generator, populatorsConfig.getString(populatorId));
                    populators.add(entry);
                }
            }
        } else {
            List<String> populatorIds = config.getStringList("populators");
            if (populatorIds != null) {
                for (String populatorId : populatorIds) {
                    BaseBlockPopulator generator = BaseBlockPopulator.loadPopulator(world, populatorId);
                    DistanceWeightedValue<BaseBlockPopulator> entry = DistanceWeightedValue.fromString(world.getLogger(), generator, "1");
                    populators.add(entry);
                }
            } else {
                world.getController().getLogger().warning("Random populator missing 'populators' section");
            }
        }
        return !populators.isEmpty();
    }

    @Override
    protected BaseBlockPopulator getPopulator(long worldSeed, int chunkX, int chunkZ) {
        return RandomUtils.getDistanceWeighted(populators, worldSeed, chunkX, chunkZ);
    }
}
