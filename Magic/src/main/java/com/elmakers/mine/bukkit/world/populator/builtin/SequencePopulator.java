package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class SequencePopulator extends BaseBlockPopulator {
    private List<BaseBlockPopulator> populators = new ArrayList<>();

    public boolean onLoad(ConfigurationSection config) {
        populators = BaseBlockPopulator.loadPopulators(world, config);
        return !populators.isEmpty();
    }

    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        for (BaseBlockPopulator populator : populators) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }
}
