package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialMap;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class ReplacePopulator extends BaseBlockPopulator {
    private static final int WARNING_INTERVAL = 10000;
    private MaterialMap replaceMap = null;
    private Map<Biome, Biome> replaceBiomes = null;
    private long lastBiomeWarning;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        replaceMap = null;
        replaceBiomes = null;
        replaceMap = controller.getMaterialSetManager().mapFromConfig(config, "replace");

        ConfigurationSection replaceBiomeSection = config.getConfigurationSection("replace_biomes");
        if (replaceBiomeSection != null) {
            replaceBiomes = new HashMap<>();
            Set<String> biomeKeys = replaceBiomeSection.getKeys(false);
            for (String biomeKey : biomeKeys) {
                String toBiomeKey = replaceBiomeSection.getString(biomeKey);
                Biome biome;
                try {
                    biome = Biome.valueOf(biomeKey.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid biome: " + biomeKey);
                    continue;
                }
                Biome toBiome;
                try {
                    toBiome = Biome.valueOf(toBiomeKey.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid biome: " + toBiomeKey);
                    continue;
                }
                replaceBiomes.put(biome, toBiome);
            }
        }

        return replaceMap != null || replaceBiomes != null;
    }

    @Override
    @Nullable
    public BlockResult populate(Block block, Random random) {
        if (replaceBiomes != null) {
            Biome newBiome = replaceBiomes.get(block.getBiome());
            if (newBiome != null) {
                try {
                    block.setBiome(newBiome);
                } catch (Exception ex) {
                    long now = System.currentTimeMillis();
                    if (now - lastBiomeWarning > WARNING_INTERVAL) {
                        lastBiomeWarning = now;
                        controller.getLogger().warning("Could not set biome to " + newBiome);
                    }
                }
            }
        }
        MaterialAndData replace =  replaceMap == null ? null : replaceMap.get(block.getType());
        if (replace == null) {
            return BlockResult.SKIP;
        }
        replace.modify(block);
        return BlockResult.CANCEL;
    }
}
