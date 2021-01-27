package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.populator.MagicBlockPopulator;

public class VoidPopulator extends MagicBlockPopulator {
    private static MaterialAndData AIR = new MaterialAndData(Material.AIR);

    @Override
    public boolean onLoad(ConfigurationSection configuration) {
        return true;
    }

    @Override
    public BlockResult populate(Block block, Random random) {
        block.setType(Material.AIR, false);
        return BlockResult.CANCEL;
    }
}
