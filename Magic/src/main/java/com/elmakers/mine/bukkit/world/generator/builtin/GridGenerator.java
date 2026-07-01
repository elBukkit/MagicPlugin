package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.OptionalGenerator;

public class GridGenerator extends OptionalGenerator {
    private BaseChunkGenerator fallbackGenerator;
    private final Map<Integer, Map<Integer, BaseChunkGenerator>> generatorMap = new HashMap<>();

    public boolean onLoad(ConfigurationSection config) {
        WorldController controller = world.getController().getWorlds();
        fallbackGenerator = config.contains("fallback") ? controller.parseGenerator(world, config, null, "fallback") : null;
        ConfigurationSection gridConfig = config.getConfigurationSection("generators");
        for (String entryKey : gridConfig.getKeys(false)) {
            ConfigurationSection entry = gridConfig.getConfigurationSection(entryKey);
            final int x = entry.getInt("x", 0);
            final int z = entry.getInt("z", 0);
            BaseChunkGenerator generator;
            if (entry.contains("generator") || entry.contains("class")) {
                generator = controller.parseGenerator(world, entry);
            } else {
                generator = controller.createGenerator(world, entryKey);
            }
            if (generator != null) {
                Map<Integer, BaseChunkGenerator> map = generatorMap.computeIfAbsent(x, m -> new HashMap<>());
                map.put(z, generator);
            }
        }
        return fallbackGenerator != null || !generatorMap.isEmpty();
    }

    @Override
    protected BaseChunkGenerator getGenerator(long worldSeed, int chunkX, int chunkZ) {
        Map<Integer, BaseChunkGenerator> map = generatorMap.get(chunkX);
        if (map == null) {
            return fallbackGenerator;
        }
        BaseChunkGenerator generator = map.get(chunkZ);
        return generator != null ? generator : fallbackGenerator;
    }
}
