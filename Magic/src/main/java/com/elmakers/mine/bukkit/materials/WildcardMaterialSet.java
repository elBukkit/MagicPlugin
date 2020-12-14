package com.elmakers.mine.bukkit.materials;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;

enum WildcardMaterialSet implements MaterialSet {
    INSTANCE;

    @Override
    public Collection<Material> getMaterials() {
        return Collections.emptyList();
    }

    @Override
    public MaterialSet not() {
        return MaterialSets.empty();
    }

    @Override
    public boolean testMaterial(Material material) {
        // Match everything
        return true;
    }

    @Override
    public boolean testBlock(Block testBlock) {
        return true;
    }

    @Override
    public boolean testItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean testMaterialAndData(MaterialAndData targetMaterial) {
        return true;
    }

    @Override
    public String toString() {
        return "(*)";
    }
}
