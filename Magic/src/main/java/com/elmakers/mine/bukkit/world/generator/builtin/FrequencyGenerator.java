package com.elmakers.mine.bukkit.world.generator.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.OptionalGenerator;

public class FrequencyGenerator extends OptionalGenerator {
    private BaseChunkGenerator generator;
    private int frequency = 2;

    public boolean onLoad(ConfigurationSection config) {
        frequency = config.getInt("frequency", frequency);
        WorldController controller = world.getController().getWorlds();
        generator = controller.parseGenerator(world, config);
        return generator != null;
    }

    protected boolean isValid(int chunkX, int chunkZ) {
        return chunkX % frequency == 0 && chunkZ % frequency == 0;
    }

    @Override
    protected BaseChunkGenerator getGenerator(WorldContext context) {
        return isValid(context.getChunkX(), context.getChunkZ()) ? generator : null;
    }
}
