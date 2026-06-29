package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class GridPopulator extends BaseBlockPopulator {
    private record GridEntry(BaseBlockPopulator populator, int x, int z) {}
    private final List<GridEntry> entries = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        entries.clear();
        ConfigurationSection gridConfig = config.getConfigurationSection("grid");
        WorldController controller = world.getController().getWorlds();
        if (gridConfig == null) {
            controller.getLogger().warning("Grid populator missing 'grid' layout");
            return false;
        }
        for (String entryKey : gridConfig.getKeys(false)) {
            ConfigurationSection entry = gridConfig.getConfigurationSection(entryKey);
            final int x = entry.getInt("x", 0);
            final int z = entry.getInt("z", 0);
            BaseBlockPopulator populator = BaseBlockPopulator.parsePopulator(world, entry);
            if (populator != null) {
                entries.add(new GridEntry(
                    populator, x, z
                ));
            }
        }
        return !entries.isEmpty();
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        for (GridEntry entry : entries) {
            entry.populator.populate(worldInfo, random, chunkX + entry.x, chunkZ + entry.z, region);
        }
    }
}
