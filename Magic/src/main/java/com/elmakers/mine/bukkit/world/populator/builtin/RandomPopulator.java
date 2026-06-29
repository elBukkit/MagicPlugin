package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.DistanceWeightedValue;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class RandomPopulator extends BaseBlockPopulator {
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

    protected BaseBlockPopulator getPopulator(long worldSeed, int chunkX, int chunkZ) {
        return RandomUtils.getDistanceWeighted(populators, worldSeed, chunkX, chunkZ);
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        BaseBlockPopulator populator = getPopulator(worldInfo.getSeed(), chunkX, chunkZ);
        if (populator != null) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        BaseBlockPopulator populator = getPopulator(location.getWorld().getSeed(), location.getChunk().getX(), location.getChunk().getZ());
        if (populator == null) {
            return null;
        }
        return populator.getPortalTargetWorld(location);
    }
}
