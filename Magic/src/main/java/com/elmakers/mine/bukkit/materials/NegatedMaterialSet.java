package com.elmakers.mine.bukkit.materials;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.google.common.collect.ImmutableList;

final class NegatedMaterialSet implements MaterialSet {
    private final @Nonnull
    MaterialSet delegate;

    public NegatedMaterialSet(MaterialSet delegate) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public MaterialSet not() {
        return delegate;
    }

    @Override
    public Collection<Material> getMaterials() {
        return ImmutableList.of();
    }

    @Override
    public boolean testMaterial(Material material) {
        return !delegate.testMaterial(material);
    }

    @Override
    public boolean testBlock(Block testBlock) {
        return !delegate.testBlock(testBlock);
    }

    @Override
    public boolean testItem(ItemStack item) {
        return !delegate.testItem(item);
    }

    @Override
    public boolean testMaterialAndData(MaterialAndData targetMaterial) {
        return !delegate.testMaterialAndData(targetMaterial);
    }

    @Override
    public String toString() {
        return "!(" + delegate + ")";
    }
}
