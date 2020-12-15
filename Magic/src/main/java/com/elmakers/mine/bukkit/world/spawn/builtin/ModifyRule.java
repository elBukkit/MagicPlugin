package com.elmakers.mine.bukkit.world.spawn.builtin;

import javax.annotation.Nonnull;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.world.spawn.SpawnResult;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class ModifyRule extends SpawnRule {
    protected EntityData entityData;

    @Override
    public void finalizeLoad(String worldName) {
        entityData = new EntityData(controller, parameters);
        logSpawnRule("Modifying " + getTargetEntityTypeName() + " in " + worldName);
    }

    @Override
    @Nonnull
    public SpawnResult onProcess(Plugin plugin, LivingEntity entity) {
        entityData.modify(entity);
        return SpawnResult.SKIP;
    }
}
