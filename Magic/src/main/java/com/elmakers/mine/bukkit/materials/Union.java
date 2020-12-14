package com.elmakers.mine.bukkit.materials;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Builder for unions of sets.
 */
public final class Union {
    private boolean wildcard = false;
    private final List<MaterialSet> sets = new ArrayList<>();

    // TODO: Should this be a set?
    private final List<MaterialAndData> materialAndDatas = new ArrayList<>();
    private final Set<Material> materials = new HashSet<>();

    Union() {
    }

    public Union add(MaterialSet set) {
        checkNotNull(set, "set");

        if (set instanceof EmptyMaterialSet) {
            // NOOP
            return this;
        } else if (set instanceof WildcardMaterialSet) {
            wildcard = true;
            return this;
        }

        if (set instanceof SimpleMaterialSet) {
            // Unpack set rather than adding it as a parent
            SimpleMaterialSet simpleSet = ((SimpleMaterialSet) set);

            // No need to recur, we assume the set was created using an
            // union.
            sets.addAll(simpleSet.parents);
            materialAndDatas.addAll(simpleSet.materialAndDatas);
            materials.addAll(simpleSet.materials);
        } else {
            // Negated or custom class
            sets.add(set);
        }

        return this;
    }

    public Union add(Material material) {
        checkNotNull(material, "material");

        materials.add(material);

        return this;
    }

    public Union add(MaterialAndData materialAndData) {
        checkNotNull(materialAndData, "materialAndData");
        checkArgument(materialAndData.isValid(),
                "Material data is not valid: %s", materialAndData);

        materialAndDatas.add(materialAndData);

        return this;
    }

    public Union addAll(Material... materials) {
        for (Material material : materials) {
            checkNotNull(material, "material");

            this.materials.add(material);
        }

        return this;
    }

    public MaterialSet build() {
        // If there is a wildcard in this union, the union is a wildcard.
        if (wildcard) {
            return MaterialSets.wildcard();
        }

        boolean needMaterialList = !materials.isEmpty();
        for (MaterialSet set : sets) {
            needMaterialList |= !set.getMaterials().isEmpty();
        }

        // Check for completely empty set
        if (!needMaterialList && sets.isEmpty() && materialAndDatas.isEmpty()) {
            return MaterialSets.empty();
        }

        // Build the new set of materials
        ImmutableSet<Material> newMaterials;
        if (needMaterialList) {
            ImmutableSet.Builder<Material> materialsBuilder;
            materialsBuilder = ImmutableSet.builder();
            materialsBuilder.addAll(this.materials);

            for (MaterialSet set : sets) {
                materialsBuilder.addAll(set.getMaterials());
            }

            newMaterials = materialsBuilder.build();
        } else {
            newMaterials = ImmutableSet.of();
        }

        // Build the new set of material datas
        ImmutableList<MaterialAndData> newMaterialAndDatas;
        if (!materialAndDatas.isEmpty()) {
            ImmutableList.Builder<MaterialAndData> materialsBuilder;
            materialsBuilder = ImmutableList.builder();

            for (MaterialAndData materialAndData : materialAndDatas) {
                if (!newMaterials.contains(materialAndData.getMaterial())) {
                    // Only keep when it is not already matched by a regular
                    // material.
                    materialsBuilder.add(materialAndData);
                }
            }

            newMaterialAndDatas = materialsBuilder.build();
        } else {
            newMaterialAndDatas = ImmutableList.of();
        }

        return new SimpleMaterialSet(
                ImmutableList.copyOf(sets),
                newMaterials, newMaterialAndDatas);
    }
}
