package com.elmakers.mine.bukkit.magic;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.block.NegatedHashSet;

public class NegatedPredicateMap extends SimpleMaterialPredicateMap {
    @Override
    public Set<Material> getLegacyMaterials() {
        return new NegatedHashSet<Material>(super.getLegacyMaterials());
    }

    @Override
    public boolean apply(BlockState state) {
        return !super.apply(state);
    }

    @Override
    public boolean apply(ItemStack is) {
        return !super.apply(is);
    }
}
