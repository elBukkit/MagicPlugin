package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class MobPopulator extends BaseBlockPopulator {
    @Override
    public boolean onLoad(ConfigurationSection config) {
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
    }
}
