package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.world.populator.MagicBlockPopulator;

public class ReplacePopulator extends MagicBlockPopulator {
    private static final int WARNING_INTERVAL = 10000;
    private Map<Material, MaterialAndData> replaceMap = null;
    private Map<Biome, Biome> replaceBiomes = null;
    private int maxY = 128;
    private int minY = 3;
    private long lastBiomeWarning;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        boolean keepBlockData = config.getBoolean("keep_block_data", false);
        replaceMap = null;
        replaceBiomes = null;

        maxY = config.getInt("max_y");
        if (maxY == 0) {
            maxY = 128;
        }
        minY = config.getInt("min_y");
        if (minY == 0) {
            minY = 3;
        }

        ConfigurationSection replaceSection = config.getConfigurationSection("replace");
        if (replaceSection != null) {
            replaceMap = new HashMap<Material, MaterialAndData>();
            Map<String, Object> replaceNodes = replaceSection.getValues(false);
            for (Entry<String, Object> replaceNode : replaceNodes.entrySet()) {
                MaterialAndData fromMaterial = new MaterialAndData(replaceNode.getKey());
                if (!fromMaterial.isValid()) {
                    controller.getLogger().warning("Invalid material key: " + replaceNode.getKey());
                    continue;
                }
                MaterialAndData toMaterial = new MaterialAndData(replaceNode.getValue().toString());
                if (!toMaterial.isValid()) {
                    controller.getLogger().warning("Invalid material key: " + replaceNode.getValue());
                    continue;
                }
                if (keepBlockData) {
                    toMaterial.setData(null);
                }
                replaceMap.put(fromMaterial.getMaterial(), toMaterial);
            }
        }

        ConfigurationSection replaceBiomeSection = config.getConfigurationSection("replace_biomes");
        if (replaceBiomeSection != null) {
            replaceBiomes = new HashMap<Biome, Biome>();
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
    public MaterialAndData populate(Block block, Random random) {
        if (block.getY() < minY || block.getY() > maxY) return null;
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
        return replaceMap == null ? null : replaceMap.get(block.getType());
    }
}
