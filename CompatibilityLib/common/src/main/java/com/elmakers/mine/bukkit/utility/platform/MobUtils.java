package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalType;

public interface MobUtils {
    boolean removePathfinderGoals(Entity entity);

    boolean setPathfinderGoal(Entity entity, GoalType goalType, Entity target, ConfigurationSection config);

    boolean setPathfinderTarget(Entity entity, Entity target, double speed);
}
