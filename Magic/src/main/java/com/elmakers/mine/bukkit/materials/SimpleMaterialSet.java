package com.elmakers.mine.bukkit.materials;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Concrete implementation of a material set.
 */
final class SimpleMaterialSet implements MaterialSet {
    protected final @Nonnull
    ImmutableList<MaterialSet> parents;
    protected final @Nonnull
    ImmutableSet<Material> materials;
    protected final @Nonnull ImmutableList<MaterialAndData> materialAndDatas;

    private transient MaterialSet inverse;

    public SimpleMaterialSet(
            @Nonnull ImmutableList<MaterialSet> parents,
            @Nonnull ImmutableSet<Material> materials,
            @Nonnull ImmutableList<MaterialAndData> materialAndDatas) {
        this.materialAndDatas = materialAndDatas;
        this.parents = parents;
        this.materials = checkNotNull(materials);
    }

    @Override
    public MaterialSet not() {
        if (inverse != null) {
            return inverse;
        }

        return inverse = new NegatedMaterialSet(this);
    }

    @Override
    public Collection<Material> getMaterials() {
        return materials;
    }

    @Override
    public boolean testMaterial(Material material) {
        // Don't use material and data here as those only match with
        // specific additional data.
        if (materials.contains(material)) {
            return true;
        }

        for (MaterialSet parent : parents) {
            if (parent.testMaterial(material)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean testBlock(Block block) {
        if (materials.contains(block.getType())) {
            return true;
        }

        for (MaterialSet parent : parents) {
            if (parent.testBlock(block)) {
                return true;
            }
        }

        for (MaterialAndData materialAndData : materialAndDatas) {
            if (materialAndData.is(block)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean testItem(ItemStack item) {
        if (materials.contains(item.getType())) {
            return true;
        }

        for (MaterialSet parent : parents) {
            if (parent.testItem(item)) {
                return true;
            }
        }

        DeprecatedUtils deprecatedUtils = CompatibilityLib.getDeprecatedUtils();
        for (MaterialAndData materialAndData : materialAndDatas) {
            // TODO: Consider item metadata
            if (materialAndData.getMaterial() == item.getType()
                    && materialAndData.getData() == deprecatedUtils.getItemDamage(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean testMaterialAndData(MaterialAndData targetMaterial) {
        if (materials.contains(targetMaterial.getMaterial())) {
            return true;
        }

        for (MaterialSet parent : parents) {
            if (parent.testMaterialAndData(targetMaterial)) {
                return true;
            }
        }

        for (MaterialAndData materialAndData : materialAndDatas) {
            // TODO: This does not properly check extra block data
            if (materialAndData.equals(targetMaterial)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        boolean first = true;
        StringBuilder sb = new StringBuilder().append('(');

        for (MaterialSet parent : parents) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(parent);
        }

        for (Material material : materials) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(material.name().toLowerCase());
        }

        for (MaterialAndData material : materialAndDatas) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(material.getKey());
        }

        return sb.append(')').toString();
    }
}
