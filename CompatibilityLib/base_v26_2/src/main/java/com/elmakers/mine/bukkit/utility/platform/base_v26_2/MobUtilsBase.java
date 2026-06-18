package com.elmakers.mine.bukkit.utility.platform.base_v26_2;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.Mob;

public class MobUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_1.MobUtilsBase {
    public MobUtilsBase(Platform platform) {
        super(platform);
    }

    @Override
    public boolean removeGoal(Entity entity, GoalType goalType) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return removeGoal(mob.getGoalSelector(), mob, entity, goalType);
    }

    @Override
    public Collection<String> getGoalDescriptions(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return null;
        }
        return getGoalDescriptions(mob.getGoalSelector());
    }

    @Override
    public boolean addGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return addGoal(mob.getGoalSelector(), mob, entity, goalType, priority, config);
    }

    @Override
    public boolean removeGoals(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        mob.getGoalSelector().getAvailableGoals().clear();
        return true;
    }
}
