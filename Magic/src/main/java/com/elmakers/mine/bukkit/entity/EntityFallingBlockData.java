package com.elmakers.mine.bukkit.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class EntityFallingBlockData extends EntityExtraData {
    @Nullable
    private MaterialAndData material;
    private boolean dropItems;
    private boolean hurtEntities;

    private EntityFallingBlockData(MaterialAndData material) {
        this.material = material;
    }

    public EntityFallingBlockData(ConfigurationSection configuration) {
        material = ConfigurationUtils.getMaterialAndData(configuration, "material");
        dropItems = configuration.getBoolean("drop_items", false);
        hurtEntities = configuration.getBoolean("hurt_entities", true);
    }

    public EntityFallingBlockData(FallingBlock fallingBlock) {
        byte data = CompatibilityUtils.getBlockData(fallingBlock);
        material = new com.elmakers.mine.bukkit.block.MaterialAndData(fallingBlock.getMaterial(), data);
        fallingBlock.setDropItem(dropItems);
        fallingBlock.setHurtEntities(hurtEntities);
    }

    @Override
    public void apply(Entity entity) {
        // Can't change a falling block after the fact.
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
