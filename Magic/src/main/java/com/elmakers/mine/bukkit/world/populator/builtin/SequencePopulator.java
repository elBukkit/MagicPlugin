package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.CompoundPopulator;

public class SequencePopulator extends CompoundPopulator {
    private List<BaseBlockPopulator> populators = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        populators = BaseBlockPopulator.loadPopulators(world, config);
        return !populators.isEmpty();
    }

    @Override
    protected List<BaseBlockPopulator> getPopulators(long worldSeed, int chunkX, int chunkZ) {
        return populators;
    }
}
