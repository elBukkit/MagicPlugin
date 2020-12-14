package com.elmakers.mine.bukkit.materials;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;

enum EmptyMaterialSet implements MaterialSet {
    INSTANCE;

    @Override
    public Collection<Material> getMaterials() {
        return Collections.emptyList();
    }

    @Override
    public MaterialSet not() {
        return MaterialSets.wildcard();
    }

    @Override
    public boolean testMaterial(Material material) {
        return false;
    }

    @Override
    public boolean testBlock(Block testBlock) {
        return false;
    }

    @Override
    public boolean testItem(ItemStack item) {
        return false;
    }

    @Override
    public boolean testMaterialAndData(MaterialAndData targetMaterial) {
        return false;
    }

    @Override
    public String toString() {
        return "()";
    }
}
