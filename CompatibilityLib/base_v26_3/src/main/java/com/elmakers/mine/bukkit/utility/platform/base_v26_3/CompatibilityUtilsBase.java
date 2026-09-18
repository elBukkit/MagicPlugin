package com.elmakers.mine.bukkit.utility.platform.base_v26_3;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class CompatibilityUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_2.CompatibilityUtilsBase {
    protected CompatibilityUtilsBase(final Platform platform) {
        super(platform);
    }

    @Override
    public boolean isSwingingArm(Entity entity) {
        if (entity == null) return false;
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        if (nms == null || !(nms instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        return ((net.minecraft.world.entity.LivingEntity)nms).isSwinging();
    }
}
