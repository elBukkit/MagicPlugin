package com.elmakers.mine.bukkit.utility.platform.v1_17_1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.base.MobUtilsBase;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class MobUtils extends MobUtilsBase {
    private final Platform platform;

    public MobUtils(Platform platform) {
        this.platform = platform;
    }

    private PathfinderMob getMob(Entity entity) {
        CraftEntity craft = (CraftEntity)entity;
        net.minecraft.world.entity.Entity nms = craft.getHandle();
        if (!(nms instanceof PathfinderMob)) {
            return null;
        }
        return (PathfinderMob)nms;
    }

    @Override
    public boolean removeGoals(Entity entity) {
        PathfinderMob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        mob.goalSelector.removeAllGoals();
        return true;
    }

    @Override
    public boolean removeGoal(Entity entity, GoalType goalType) {
        PathfinderMob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        // TODO: Is there a cleaner way?
        Goal targetGoal = getGoal(goalType, mob, new MemoryConfiguration());
        Collection<WrappedGoal> available = mob.goalSelector.getAvailableGoals();
        List<Goal> found = new ArrayList<>();
        for (WrappedGoal wrappedGoal : available) {
            if (targetGoal.getClass().isAssignableFrom(wrappedGoal.getGoal().getClass())) {
                found.add(wrappedGoal.getGoal());
            }
        }
        for (Goal removeGoal : found) {
            mob.goalSelector.removeGoal(removeGoal);
        }
        return true;
    }

    @Override
    public boolean addGoal(Entity entity, GoalType goalType, Entity target, ConfigurationSection config) {
        PathfinderMob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        CraftEntity craft = (CraftEntity)entity;
        net.minecraft.world.entity.Entity nms = craft.getHandle();
        if (!(nms instanceof PathfinderMob)) {
            return false;
        }
        GoalSelector goals = mob.goalSelector;
        Goal goal = getGoal(goalType, mob, config);
        if (goal == null) {
            return false;
        }
        int priority = config.getInt("priority", 0);
        goals.addGoal(priority, goal);
        return true;
    }

    private Goal getGoal(GoalType goalType, PathfinderMob mob, ConfigurationSection config) {
        double speed = config.getDouble("speed", 1);
        double sprintSpeed = config.getDouble("sprint_speed", 1);
        String classType = config.getString("entity_class", "player");
        float distance = (float)config.getDouble("distance", 16);
        switch (goalType) {
            case AVOID_ENTITY:
                return getAvoidEntityGoal(mob, classType, distance, sprintSpeed, sprintSpeed);
            case BEG:
                if (mob instanceof Wolf) {
                    return new BegGoal((Wolf)mob, distance);
                }
                return null;
            case BREAK_DOOR:
                return new BreakDoorGoal(mob, difficulty -> true);
            case BREATHE_AIR:
                return new BreathAirGoal(mob);
            case BREED:
                if (mob instanceof  Animal) {
                    return new BreedGoal((Animal)mob, speed);
                }
                return null;
            case EAT_BLOCK:
                return new EatBlockGoal(mob);
            case FLEE_SUN:
                return new FleeSunGoal(mob, speed);
            case FLOAT:
                return new FloatGoal(mob);
            case FOLLOW_BOAT:
                return new FollowBoatGoal(mob);
            case FOLLOW_MOB:
                return new FollowMobGoal(mob, speed, distance, (float)config.getDouble("area_size", 7));
            case GOLEM_RANDOM_STROLL_IN_VILLAGE:
                return new GolemRandomStrollInVillageGoal(mob, speed);
            case INTERACT:
                return getInteractGoal(mob, classType, distance, (float)config.getDouble("probability", 1));
            case LEAP_AT_TARGET:
                return new LeapAtTargetGoal(mob, (float)config.getDouble("y_offset", 0.4));
            case LOOK_AT_PLAYER:
                return getLookAtPlayerGoal(mob, classType, distance, (float)config.getDouble("probability", 1), config.getBoolean("horizontal"));
            case PANIC:
                return new PanicGoal(mob, speed);
            default:
                platform.getLogger().warning("Unsupported goal type: " + goalType);
                return null;
        }
    }

    private Class<? extends LivingEntity> getMobClass(String classType) {
        switch (classType) {
            case "player":
                return Player.class;
            case "livingentity":
            case "living_entity":
                return LivingEntity.class;
            case "monster":
                return Monster.class;
            case "animal":
                return Animal.class;
            case "villager":
                return Villager.class;
            default:
                // TODO: Implement more.. :(
                return null;
        }
    }

    private Goal getLookAtPlayerGoal(PathfinderMob mob, String classType, float distance, float probability, boolean horizontal) {
        Class<? extends LivingEntity> mobClass = getMobClass(classType);
        if (mobClass == null) {
            platform.getLogger().warning("Unsupported entity_class in interact goal: " + classType);
            return null;
        }
        return new LookAtPlayerGoal(mob, mobClass, distance, probability, horizontal);
    }

    private Goal getInteractGoal(PathfinderMob mob, String classType, float distance, float probability) {
        Class<? extends LivingEntity> mobClass = getMobClass(classType);
        if (mobClass == null) {
            platform.getLogger().warning("Unsupported entity_class in interact goal: " + classType);
            return null;
        }
        return new InteractGoal(mob, mobClass, distance, probability);
    }

    private Goal getAvoidEntityGoal(PathfinderMob mob, String classType, float distance, double speed, double sprintSpeed) {
        switch (classType) {
            case "player":
                return new AvoidEntityGoal<>(mob, Player.class, distance, speed, sprintSpeed);
            case "livingentity":
            case "living_entity":
                return new AvoidEntityGoal<>(mob, LivingEntity.class, distance, speed, sprintSpeed);
            case "monster":
                return new AvoidEntityGoal<>(mob, Monster.class, distance, speed, sprintSpeed);
            case "animal":
                return new AvoidEntityGoal<>(mob, Animal.class, distance, speed, sprintSpeed);
            case "villager":
                return new AvoidEntityGoal<>(mob, Villager.class, distance, speed, sprintSpeed);
            default:
                // TODO: Implement more.. :(
                platform.getLogger().warning("Unsupported entity_class in avoid_entity goal: " + classType);
                return null;
        }
    }

    @Override
    public boolean setPathfinderTarget(Entity entity, Entity target, double speed) {
        if (entity == null || target == null) return false;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)entity).getHandle();
        net.minecraft.world.entity.Entity nmstarget = ((CraftEntity)target).getHandle();
        if (!(nmsEntity instanceof PathfinderMob)) {
            return false;
        }

        PathfinderMob pathfinder = (PathfinderMob)nmsEntity;
        pathfinder.getNavigation().moveTo(nmstarget, speed);
        return true;
    }
}
