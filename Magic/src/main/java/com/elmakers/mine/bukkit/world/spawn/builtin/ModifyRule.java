package com.elmakers.mine.bukkit.world.spawn.builtin;

import javax.annotation.Nullable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class ModifyRule extends SpawnRule {
    protected EntityData entityData;

    @Override
    public void finalizeLoad(String worldName) {
        entityData = new EntityData(controller, parameters);
        controller.info(" Modifying : " + getTargetEntityTypeName() + " at y > " + minY
                + " at a " + (percentChance * 100) + "% chance");
    }

    @Override
    @Nullable
    public LivingEntity onProcess(Plugin plugin, LivingEntity entity) {
        entityData.modify(entity);
        return null;
    }
}
