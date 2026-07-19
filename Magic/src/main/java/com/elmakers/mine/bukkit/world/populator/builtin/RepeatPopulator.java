package com.elmakers.mine.bukkit.world.populator.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.OptionalPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class RepeatPopulator extends OptionalPopulator {
    private BaseBlockPopulator populator;
    private IntegerRange repeat;

    public boolean onLoad(ConfigurationSection config) {
        repeat = IntegerRange.fromConfig(getLogger(), config, "repeat", 2, 2);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    @Override
    protected BaseBlockPopulator getPopulator(WorldContext context) {
        return populator;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final int repeat = this.repeat.getRandom(context.getRandom());
        for (int i = 0; i < repeat; i++) {
            populator.updateAndPopulate(context);
        }
    }
}
