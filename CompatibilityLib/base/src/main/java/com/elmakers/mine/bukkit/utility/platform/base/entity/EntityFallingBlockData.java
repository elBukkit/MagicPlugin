package com.elmakers.mine.bukkit.utility.platform.base.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class EntityFallingBlockData extends EntityExtraData {
    @Nullable
    private MaterialAndData material;
    private boolean dropItems;
    private boolean hurtEntities;

    private EntityFallingBlockData(MaterialAndData material) {
        this.material = material;
    }

    public EntityFallingBlockData(ConfigurationSection configuration) {
        // material = ConfigurationUtils.getMaterialAndData(configuration, "material");
        dropItems = configuration.getBoolean("drop_items", false);
        hurtEntities = configuration.getBoolean("hurt_entities", true);
    }

    public EntityFallingBlockData(FallingBlock fallingBlock, MageController controller) {
        CompatibilityUtils compatibilityUtils = getPlatform().getCompatibilityUtils();
        Material material = compatibilityUtils.getMaterial(fallingBlock);
        String blockData = compatibilityUtils.getBlockData(fallingBlock);
        if (blockData != null) {
            this.material = controller.createMaterialAndData(material, blockData);
        } else {
            byte data = compatibilityUtils.getLegacyBlockData(fallingBlock);
            this.material = controller.createMaterialAndData(material, data);
        }
        fallingBlock.setDropItem(dropItems);
        fallingBlock.setHurtEntities(hurtEntities);
    }

    @Override
    public void apply(Entity entity) {
        // Can't change a falling block after the fact.
    }

    @Override
    public void setMaterialAndData(@Nonnull MaterialAndData material) {
        this.material = material;
    }

    @Nullable
    @Override
    public MaterialAndData getMaterialAndData() {
        return material;
    }

    @Nullable
    @Override
    public Material getMaterial() {
        return material == null ? null : material.getMaterial();
    }

    @Override
    public byte getMaterialData() {
        if (material == null) {
            return 0;
        }

        Byte data = material.getBlockData();
        return data == null ? 0 : data;
    }
}
