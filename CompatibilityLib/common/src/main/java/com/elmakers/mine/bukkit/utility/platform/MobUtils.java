package com.elmakers.mine.bukkit.utility.platform;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalConfiguration;
import com.elmakers.mine.bukkit.mob.GoalType;

public interface MobUtils {
    boolean removeGoals(Entity entity);

    boolean removeGoal(Entity entity, GoalType goalType);

    Collection<String> getGoalDescriptions(Entity entity);

    boolean addGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config);

    boolean addGoal(Entity entity, GoalConfiguration goal);

    boolean removeTargetGoals(Entity entity);

    boolean removeTargetGoal(Entity entity, GoalType goalType);

    Collection<String> getTargetGoalDescriptions(Entity entity);

    boolean addTargetGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config);

    boolean addTargetGoal(Entity entity, GoalConfiguration goal);

    boolean setPathfinderTarget(Entity entity, Entity target, double speed);
}
