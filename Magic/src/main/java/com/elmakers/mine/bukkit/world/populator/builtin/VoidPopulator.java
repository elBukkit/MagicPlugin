package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.UnknownNullability;

import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.populator.SimpleBlockPopulator;

public class VoidPopulator extends SimpleBlockPopulator {
    @Override
    public boolean onLoad(ConfigurationSection configuration) {
        return true;
    }

    @Override
    public BlockResult populate(@UnknownNullability BlockState block, Random random) {
        // TODO: Feels like this should be using LimitedRegion.setBlockData somehow
        block.getBlock().setType(Material.AIR, false);

        return BlockResult.CANCEL;
    }
}
