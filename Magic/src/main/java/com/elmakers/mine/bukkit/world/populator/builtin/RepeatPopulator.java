package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class RepeatPopulator extends BaseBlockPopulator {
    private BaseBlockPopulator populator;
    private int minRepeat = 2;
    private int maxRepeat = 2;

    public boolean onLoad(ConfigurationSection config) {
        minRepeat = config.getInt("min_repeat", minRepeat);
        maxRepeat = config.getInt("max_repeat", maxRepeat);
        String populatorId = config.getString("populator");
        populator = BaseBlockPopulator.loadPopulator(world, populatorId);
        return populator != null;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int repeat = RandomUtils.range(random, minRepeat, maxRepeat);
        for (int i = 0; i < repeat; i++) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        return populator.getPortalTargetWorld(location);
    }
}
