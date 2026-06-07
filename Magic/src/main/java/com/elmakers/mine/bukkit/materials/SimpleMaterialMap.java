package com.elmakers.mine.bukkit.materials;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialMap;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.collect.ImmutableList;

public class SimpleMaterialMap implements MaterialMap {
    private final @Nonnull Map<Material, ? extends MaterialAndData> materials;
    private transient MaterialSet inverse;

    public SimpleMaterialMap(Map<Material, ? extends MaterialAndData> materialMap) {
        materials = materialMap;
    }

    @Override
    public MaterialAndData get(Material key) {
        return materials.get(key);
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
        return materials.keySet();
    }

    @Override
    public Collection<MaterialAndData> getMaterialsWithData() {
        ImmutableList.Builder<MaterialAndData> allBuilder = ImmutableList.builder();
        for (Material material : getMaterials()) {
            allBuilder.add(ConfigurationUtils.toMaterialAndData(material));
        }
        return allBuilder.build();
    }

    @Override
    public boolean testMaterial(Material material) {
        return materials.containsKey(material);
    }

    @Override
    public boolean testBlock(Block testBlock) {
        return testMaterial(testBlock.getType());
    }

    @Override
    public boolean testItem(ItemStack item) {
        return testMaterial(item.getType());
    }

    @Override
    public boolean testMaterialAndData(MaterialAndData targetMaterial) {
        return testMaterial(targetMaterial.getMaterial());
    }
}
