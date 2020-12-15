package com.elmakers.mine.bukkit.world.spawn.builtin;

import javax.annotation.Nonnull;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.world.spawn.SpawnResult;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class EquipmentRule extends SpawnRule {
    protected EntityData equipment;

    @Override
    public void finalizeLoad(String worldName) {
        equipment = new EntityData(controller, parameters);
        logSpawnRule("Replacing equipment of " + getTargetEntityTypeName() + " in " + worldName);
    }

    @Override
    @Nonnull
    public SpawnResult onProcess(Plugin plugin, LivingEntity entity) {
        equipment.copyEquipmentTo(entity);
        return SpawnResult.SKIP;
    }
}
