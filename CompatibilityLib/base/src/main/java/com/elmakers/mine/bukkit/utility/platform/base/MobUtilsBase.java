package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalConfiguration;
import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;

public class MobUtilsBase implements MobUtils {
    @Override
    public boolean removeGoals(Entity entity) {
        return false;
    }

    @Override
    public boolean removeGoal(Entity entity, GoalType goalType) {
        return false;
    }

    @Override
    public Collection<String> getGoalDescriptions(Entity entity) {
        return null;
    }

    @Override
    public boolean addGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config) {
        return false;
    }

    @Override
    public boolean addGoal(Entity entity, GoalConfiguration goal) {
        return addGoal(entity, goal.getGoalType(), goal.getPriority(), goal.getConfiguration());
    }

    @Override
    public boolean removeTargetGoals(Entity entity) {
        return false;
    }

    @Override
    public boolean removeTargetGoal(Entity entity, GoalType goalType) {
        return false;
    }

    @Override
    public Collection<String> getTargetGoalDescriptions(Entity entity) {
        return null;
    }

    @Override
    public boolean addTargetGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config) {
        return false;
    }

    @Override
    public boolean addTargetGoal(Entity entity, GoalConfiguration goal) {
        return addTargetGoal(entity, goal.getGoalType(), goal.getPriority(), goal.getConfiguration());
    }

    @Override
    public boolean setPathfinderTarget(Entity entity, Entity target, double speed) {
        return false;
    }
}
