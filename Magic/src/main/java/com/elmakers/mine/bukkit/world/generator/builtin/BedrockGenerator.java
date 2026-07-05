package com.elmakers.mine.bukkit.world.generator.builtin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.GeneratorContext;

public class BedrockGenerator extends BaseChunkGenerator {
    @Override
    public boolean onLoad(ConfigurationSection config) {
        return true;
    }

    @Override
    public void generate(GeneratorContext context) {
        final int bedrockLevel = world.getBedrockLevel();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                context.setBlock(x, bedrockLevel, z, Material.BEDROCK);
            }
        }
    }
}
