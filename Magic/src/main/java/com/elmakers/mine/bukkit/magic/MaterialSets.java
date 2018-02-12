package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

public class MaterialSets {
    @Nullable
    public static Set<Material> toLegacy(@Nullable MaterialSet v) {
        return v == null ? null : ImmutableSet.copyOf(v.getMaterials());
    }

    private static enum WildcardMaterialSet implements MaterialSet {
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
    };

    /**
     * @return A material set that matches all materials.
     */
    public static MaterialSet wildcard() {
        return WildcardMaterialSet.INSTANCE;
    }

    private static enum EmptyMaterialSet implements MaterialSet {
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
    }

    private static final class NegatedMaterialSet implements MaterialSet {
        private final @Nonnull MaterialSet delegate;

        public NegatedMaterialSet(MaterialSet delegate) {
            this.delegate = checkNotNull(delegate, "delegate");
        }

        @Override
        public MaterialSet not() {
            return delegate;
        }

        @Override
        public Collection<Material> getMaterials() {
            return delegate.getMaterials();
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
    }

    /**
     * Concrete implementation of a material set.
     */
    private static final class SimpleMaterialSet implements MaterialSet {
        private final @Nonnull ImmutableList<MaterialSet> parents;
        private final @Nonnull ImmutableSet<Material> materials;
        private final @Nonnull ImmutableList<MaterialAndData> materialAndDatas;

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

            for (MaterialAndData materialAndData : materialAndDatas) {
                // TODO: Consider item metadata
                if (materialAndData.getMaterial() == item.getType()
                        && materialAndData.getData() == item.getDurability()) {
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

    /**
     * Builder for unions of sets.
     */
    public static final class Union {
        private boolean wildcard = false;
        private final List<MaterialSet> sets = new ArrayList<>();

        // TODO: Should this be a set?
        private final List<MaterialAndData> materialAndDatas = new ArrayList<>();
        private final Set<Material> materials = new HashSet<>();

        private Union() {
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

        public Union addAll(Material... materials) {
            for (Material material : materials) {
                checkNotNull(material, "material");

                this.materials.add(material);
            }

            return this;
        }

        public Union add(MaterialAndData materialAndData) {
            checkNotNull(materialAndData, "materialAndData");
            checkArgument(materialAndData.isValid(),
                    "Material data is not valid: %s", materialAndData);

            materialAndDatas.add(materialAndData);

            return this;
        }

        public MaterialSet build() {
            boolean needMaterialList = !materials.isEmpty();
            for (MaterialSet set : sets) {
                needMaterialList |= !set.getMaterials().isEmpty();
            }

            // No materials and a wildcard available
            if (!needMaterialList) {
                if (wildcard) {
                    return wildcard();
                } else if (sets.isEmpty() && materialAndDatas.isEmpty()) {
                    return empty();
                }
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

            // TODO: Don't bother with the rest when a wildcard is specified

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
}
