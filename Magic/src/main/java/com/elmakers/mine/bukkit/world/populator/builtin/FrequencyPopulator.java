package com.elmakers.mine.bukkit.world.populator.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.OptionalPopulator;

public class FrequencyPopulator extends OptionalPopulator {
    private BaseBlockPopulator populator;
    private int frequency = 2;

    public boolean onLoad(ConfigurationSection config) {
        frequency = config.getInt("frequency", frequency);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    @Override
    protected BaseBlockPopulator getPopulator(long worldSeed, int chunkX, int chunkZ) {
        return isValid(chunkX, chunkZ) ? populator : null;
    }

    protected boolean isValid(int chunkX, int chunkZ) {
        return chunkX % frequency == 0 && chunkZ % frequency == 0;
    }
}
