package com.elmakers.mine.bukkit.utility.platform.v1_17_1;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.base.MobUtilsBase;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class MobUtils extends MobUtilsBase {
    private final Platform platform;

    public MobUtils(Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean removePathfinderGoals(Entity entity) {
        CraftEntity craft = (CraftEntity)entity;
        net.minecraft.world.entity.Entity nms = craft.getHandle();
        if (!(nms instanceof PathfinderMob)) {
            return false;
        }
        PathfinderMob mob = (PathfinderMob)nms;
        mob.goalSelector.removeAllGoals();
        return true;
    }

    @Override
    public boolean setPathfinderGoal(Entity entity, GoalType goalType, Entity target, ConfigurationSection config) {
        CraftEntity craft = (CraftEntity)entity;
        net.minecraft.world.entity.Entity nms = craft.getHandle();
        if (!(nms instanceof PathfinderMob)) {
            return false;
        }

        /*
        CraftEntity craftTarget = (CraftEntity)target;
        net.minecraft.world.entity.Entity nmsTarget = craftTarget == null ? null : craftTarget.getHandle();
        if (!(nmsTarget instanceof LivingEntity)) {
            nmsTarget = null;
        }
        LivingEntity livingTarget = (LivingEntity)nmsTarget;
        */

        PathfinderMob mob = (PathfinderMob)nms;
        GoalSelector goals = mob.goalSelector;
        int priority = config.getInt("priority", 0);
        double speed = config.getDouble("speed", 1);
        double sprintSpeed = config.getDouble("sprint_speed", 1);
        Goal goal;
        switch (goalType) {
            case AVOID_ENTITY:
                String classType = config.getString("entity_class", "player");
                double distance = config.getDouble("distance", 16);
                goal = getAvoidEntityGoal(mob, classType, (float)distance, sprintSpeed, sprintSpeed);
                break;
            case PANIC:
                goal = new PanicGoal(mob, speed);
                break;
            case FLEE_SUN:
                goal = new FleeSunGoal(mob, speed);
                break;
            default:
                platform.getLogger().warning("Unsupported goal type: " + goalType);
                goal = null;
        }
        if (goal == null) {
            return false;
        }
        goals.addGoal(priority, goal);
        return true;
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
