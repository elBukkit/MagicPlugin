package com.elmakers.mine.bukkit.utility;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MaterialPredicate;

public class MaterialDataValuePredicate implements MaterialPredicate {
    private final short data;

    public MaterialDataValuePredicate(short data) {
        this.data = data;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean apply(BlockState block) {
        return block.getRawData() == data;
    }

    @Override
    public boolean apply(ItemStack is) {
        return is.getDurability() == data;
    }
}
