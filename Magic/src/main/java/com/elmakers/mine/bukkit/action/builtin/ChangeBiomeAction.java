package com.elmakers.mine.bukkit.action.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ChangeBiomeAction extends BaseSpellAction {
    private Biome biome;
    private Map<Biome, Biome> biomeMap;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        String biomeKey = parameters.getString("biome", "");
        if (!biomeKey.isEmpty()) {
            try {
                biome = Biome.valueOf(biomeKey.toUpperCase());
            } catch (Exception biomeEx) {
                context.getLogger().warning("Invalid biome: " + biomeKey);
            }
        }
        ConfigurationSection replaceConfiguration = parameters.getConfigurationSection("biome_replacements");
        if (replaceConfiguration != null) {
            biomeMap = new HashMap<>();
            Set<String> fromKeys = replaceConfiguration.getKeys(false);
            for (String fromKey : fromKeys) {
                Biome fromBiome;
                try {
                    fromBiome = Biome.valueOf(fromKey.toUpperCase());
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid biome replacement (from): " + fromKey);
                    continue;
                }
                String toKey = replaceConfiguration.getString(fromKey);
                Biome toBiome;
                try {
                    toBiome = Biome.valueOf(toKey.toUpperCase());
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid biome replacement (from): " + fromKey);
                    continue;
                }
                biomeMap.put(fromBiome, toBiome);
            }
        }
        if (biome == null && biomeMap == null) {
            context.getLogger().warning("Biome action missing either biome or biome_replacements parameters");
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
        Biome targetBiome = biome;
        if (biomeMap != null) {
            targetBiome = biomeMap.get(block.getBiome());
        }
        if (targetBiome == null) {
            return SpellResult.NO_TARGET;
        }
        block.setBiome(targetBiome);
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
