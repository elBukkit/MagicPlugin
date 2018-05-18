package com.elmakers.mine.bukkit.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class EntityFallingBlockData extends EntityExtraData {
    @Nullable
    private MaterialAndData material;

    private EntityFallingBlockData(MaterialAndData material) {
        this.material = material;
    }

    public EntityFallingBlockData(ConfigurationSection configuration) {
        material = ConfigurationUtils.getMaterialAndData(configuration, "material");
    }

    @SuppressWarnings("deprecation")
    public EntityFallingBlockData(FallingBlock fallingBlock) {
       material = new com.elmakers.mine.bukkit.block.MaterialAndData(fallingBlock.getMaterial(), fallingBlock.getBlockData());
    }

    @Override
    public void apply(Entity entity) {
        // Can't change a falling block after the fact.
    }

    @Override
    public EntityExtraData clone() {
        EntityFallingBlockData copy = new EntityFallingBlockData(this.material);
        return copy;
    }

    public void setMaterialAndData(@Nonnull MaterialAndData material) {
        this.material = material;
    }

    @Nullable
    public MaterialAndData getMaterialAndData() {
        return material;
    }

    @Nullable
    public Material getMaterial() {
        return material == null ? null : material.getMaterial();
    }

    public byte getData() {
        if (material == null) {
            return 0;
        }

        Byte data = material.getBlockData();
        return data == null ? 0 : data;
    }
}
