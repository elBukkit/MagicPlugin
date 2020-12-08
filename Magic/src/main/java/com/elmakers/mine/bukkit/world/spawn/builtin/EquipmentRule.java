package com.elmakers.mine.bukkit.world.spawn.builtin;

import javax.annotation.Nullable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class EquipmentRule extends SpawnRule {
    protected EntityData equipment;

    @Override
    public void finalizeLoad(String worldName)
    {
        equipment = new EntityData(controller, parameters);
        controller.getLogger().info(" Replacing equipment of : " + getTargetEntityTypeName() + " at y > " + minY
                + " at a " + (percentChance * 100) + "% chance");
    }

    @Override
    @Nullable
    public LivingEntity onProcess(Plugin plugin, LivingEntity entity) {
        equipment.copyEquipmentTo(entity);
        return null;
    }
}
