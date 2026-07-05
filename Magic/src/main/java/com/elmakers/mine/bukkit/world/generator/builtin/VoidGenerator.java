package com.elmakers.mine.bukkit.world.generator.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.GeneratorContext;

public class VoidGenerator extends BaseChunkGenerator {
    @Override
    public boolean onLoad(ConfigurationSection config) {
        return true;
    }

    @Override
    public void generate(GeneratorContext context) {

    }
}
