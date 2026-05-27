package com.elmakers.mine.bukkit.utility.platform.base_v26_1.entity;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public class EntityBlockDisplay extends EntityDisplay {
    protected MaterialAndData material;

    public EntityBlockDisplay(ConfigurationSection configuration, MageController controller) {
        super(configuration, controller);

        String materialKey = configuration.getString("block");
        if (materialKey != null && !materialKey.isEmpty()) {
            material = controller.createMaterialAndData(materialKey);
        }
    }

    public EntityBlockDisplay(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof BlockDisplay) {
            BlockDisplay display = (BlockDisplay) entity;
            BlockData blockData = display.getBlock();
            Material material = blockData.getMaterial();
            String blockExtraData = blockData.getAsString();
            this.material = controller.createMaterialAndData(material, blockExtraData);
        }
    }

    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof BlockDisplay) {
            BlockDisplay display = (BlockDisplay) entity;
            if (material != null) {
                String blockDataString = material.getModernBlockData();
                if (blockDataString != null && !blockDataString.isEmpty()) {
                    BlockData blockData = PlatformInterpreter.getPlatform().getPlugin().getServer().createBlockData(blockDataString);
                    display.setBlock(blockData);
                }
            }
        }
    }
}
