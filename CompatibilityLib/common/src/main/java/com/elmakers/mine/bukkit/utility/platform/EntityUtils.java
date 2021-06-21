package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.Art;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;

public interface EntityUtils {
    EntityExtraData getExtraData(MageController controller, Entity entity);

    EntityExtraData getExtraData(MageController controller, EntityType entityType, ConfigurationSection configuration);

    EntityExtraData getPaintingData(Art art, BlockFace direction);

    EntityExtraData getItemFrameData(ItemStack item, BlockFace direction, Rotation rotation);
}
