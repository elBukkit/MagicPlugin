package com.elmakers.mine.bukkit.utility.platform.base;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;

public class MobUtilsBase implements MobUtils {
    @Override
    public boolean removePathfinderGoals(Entity entity) {
        return false;
    }

    @Override
    public boolean setPathfinderGoal(Entity entity, GoalType goalType, Entity target, ConfigurationSection config) {
        return false;
    }

    @Override
    public boolean setPathfinderTarget(Entity entity, Entity target, double speed) {
        return false;
    }
}
