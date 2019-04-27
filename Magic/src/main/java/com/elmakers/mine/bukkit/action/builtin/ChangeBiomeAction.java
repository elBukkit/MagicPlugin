package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ChangeBiomeAction extends BaseSpellAction {
    private Biome biome;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        String biomeKey = parameters.getString("biome", "");
        try {
            biome = Biome.valueOf(biomeKey.toUpperCase());
        } catch (Exception biomeEx) {
            context.getLogger().warning("Invalid biome: " + biomeKey);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (biome == null) {
            return SpellResult.FAIL;
        }
        Block block = context.getTargetBlock();
        block.setBiome(biome);
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
