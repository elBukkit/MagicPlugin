package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.OptionalPopulator;

public class RepeatPopulator extends OptionalPopulator {
    private BaseBlockPopulator populator;
    private IntegerRange repeat;

    public boolean onLoad(ConfigurationSection config) {
        repeat = IntegerRange.fromConfig(getLogger(), config, "repeat", 2, 2);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    @Override
    protected BaseBlockPopulator getPopulator(long worldSeed, int chunkX, int chunkZ) {
        return populator;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int repeat = this.repeat.getRandom(random);
        for (int i = 0; i < repeat; i++) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }
}
