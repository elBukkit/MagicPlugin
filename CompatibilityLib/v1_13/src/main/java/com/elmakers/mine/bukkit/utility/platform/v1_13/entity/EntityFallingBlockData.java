package com.elmakers.mine.bukkit.utility.platform.v1_13.entity;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.SpawnedEntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public class EntityFallingBlockData extends com.elmakers.mine.bukkit.utility.platform.base.entity.EntityFallingBlockData {
    public EntityFallingBlockData(ConfigurationSection configuration, MageController controller) {
        super(configuration, controller);
    }

    public EntityFallingBlockData(Entity fallingBlock, MageController controller) {
        super(fallingBlock, controller);
    }

    @Override
    public SpawnedEntityExtraData spawn(Location location) {
        String blockDataString = getBlockData();
        if (blockDataString != null && !blockDataString.isEmpty()) {
            BlockData blockData = PlatformInterpreter.getPlatform().getPlugin().getServer().createBlockData(blockDataString);
            Entity newEntity = location.getWorld().spawnFallingBlock(location, blockData);
            return new SpawnedEntityExtraData(newEntity, true);
        }
        return super.spawn(location);
    }
}
