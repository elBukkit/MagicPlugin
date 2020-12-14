package com.elmakers.mine.bukkit.materials;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.google.common.collect.ImmutableSet;

public class MaterialSets {
    @Nullable
    public static Set<Material> toLegacy(@Nullable MaterialSet v) {
        return v == null ? null : toLegacyNN(v);
    }

    @Nonnull
    public static Set<Material> toLegacyNN(@Nonnull MaterialSet v) {
        return ImmutableSet.copyOf(v.getMaterials());
    }

    /**
     * @return A material set that matches all materials.
     */
    public static MaterialSet wildcard() {
        return WildcardMaterialSet.INSTANCE;
    }

    public static MaterialSet empty() {
        return EmptyMaterialSet.INSTANCE;
    }

    public static MaterialSet union(MaterialSet left, MaterialSet right) {
        // Happy path
        if (left == empty()) {
            return right;
        } else if (right == empty()) {
            return left;
        }

        return unionBuilder().add(left).add(right).build();
    }

    public static MaterialSet union(MaterialSet left, MaterialAndData right) {
        return unionBuilder().add(left).add(right).build();
    }

    public static MaterialSet union(
            MaterialSet left,
            Material... materials) {
        return unionBuilder().add(left).addAll(materials).build();
    }

    public static Union unionBuilder() {
        return new Union();
    }
}
