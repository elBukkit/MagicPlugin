package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.utility.platform.base_v1_17_0.MobUtilsBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class MobUtils extends MobUtilsBase {
    private final Platform platform;

    public MobUtils(Platform platform) {
        this.platform = platform;
    }

    @Override
    public Entity spawnWithData(EntityType entityType, Location location, Object data) {
        if (entityType == null) return null;
        platform.getNBTUtils().setString(data, "id", entityType.getKey().toString());
        ServerLevel nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        net.minecraft.world.entity.Entity nmsEntity = net.minecraft.world.entity.EntityType.loadEntityRecursive((CompoundTag)data, nmsWorld, (e) -> {
            e.setXRot(location.getPitch());
            e.setYRot(location.getYaw());
            e.setPos(location.getX(), location.getY(), location.getZ());
            return e;
        });
        if (nmsEntity == null) return null;
        return nmsEntity.getBukkitEntity();
    }
}
