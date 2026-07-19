package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.CompoundPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class GridPopulator extends CompoundPopulator {
    private record GridEntry(BaseBlockPopulator populator, int x, int z) {}
    private final List<GridEntry> entries = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        entries.clear();
        ConfigurationSection gridConfig = config.getConfigurationSection("populators");
        WorldController controller = world.getController().getWorlds();
        if (gridConfig == null) {
            controller.getLogger().warning("Grid populator missing 'populators' layout");
            return false;
        }
        for (String entryKey : gridConfig.getKeys(false)) {
            ConfigurationSection entry = gridConfig.getConfigurationSection(entryKey);
            final int x = entry.getInt("x", 0);
            final int z = entry.getInt("z", 0);
            BaseBlockPopulator populator;
            if (entry.contains("populator") || entry.contains("class")) {
                populator = BaseBlockPopulator.parsePopulator(world, entry);
            } else {
                populator = BaseBlockPopulator.loadPopulator(world, entryKey);
            }

            if (populator != null) {
                entries.add(new GridEntry(
                    populator, x, z
                ));
            }
        }
        return !entries.isEmpty();
    }

    @Override
    protected List<BaseBlockPopulator> getPopulators(WorldContext context) {
        return entries.stream().map(entry -> entry.populator).toList();
    }

    @Override
    protected void populate(PopulatorContext context) {
        for (GridEntry entry : entries) {
            entry.populator.updateAndPopulate(context);
        }
    }
}
