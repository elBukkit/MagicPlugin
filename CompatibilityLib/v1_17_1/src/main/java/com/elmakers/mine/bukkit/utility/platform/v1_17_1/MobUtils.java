package com.elmakers.mine.bukkit.utility.platform.v1_17_1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.mob.GoalConfiguration;
import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.base.MobUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal.IdleGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal.MagicFollowOwnerGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal.MagicGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal.RequirementsGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal.TriggerGoal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class MobUtils extends MobUtilsBase {
    private final Platform platform;

    public MobUtils(Platform platform) {
        this.platform = platform;
    }

    private net.minecraft.world.entity.Entity getNMS(Entity entity) {
        if (entity == null) return null;
        CraftEntity craft = (CraftEntity) entity;
        return craft.getHandle();
    }

    private Mob getMob(Entity entity) {
        net.minecraft.world.entity.Entity nms = getNMS(entity);
        if (!(nms instanceof Mob)) {
            return null;
        }
        return (Mob)nms;
    }

    @Override
    public boolean removeGoals(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        mob.goalSelector.removeAllGoals();
        return true;
    }

    @Override
    public boolean removeTargetGoals(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        mob.targetSelector.removeAllGoals();
        return true;
    }

    protected boolean removeGoal(GoalSelector selector, Mob mob, Entity entity, GoalType goalType) {
        // TODO: Is there a cleaner way?
        try {
            Goal targetGoal = getGoal(goalType, entity, mob, new MemoryConfiguration());
            Collection<WrappedGoal> available = selector.getAvailableGoals();
            List<Goal> found = new ArrayList<>();
            for (WrappedGoal wrappedGoal : available) {
                if (targetGoal.getClass().isAssignableFrom(wrappedGoal.getGoal().getClass())) {
                    found.add(wrappedGoal.getGoal());
                }
            }
            for (Goal removeGoal : found) {
                selector.removeGoal(removeGoal);
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error removing goal: " + goalType + " from " + entity.getType(), ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeGoal(Entity entity, GoalType goalType) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return removeGoal(mob.goalSelector, mob, entity, goalType);
    }

    @Override
    public boolean removeTargetGoal(Entity entity, GoalType goalType) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return removeGoal(mob.targetSelector, mob, entity, goalType);
    }

    public String getGoalParentDescriptions(Goal goal) {
        List<String> parentClasses = null;
        Class<?> superClass = goal.getClass().getSuperclass();
        while (superClass != null && superClass != Goal.class && superClass != MagicGoal.class && superClass != TargetGoal.class) {
            if (parentClasses == null) {
                parentClasses = new ArrayList<>();
            }
            parentClasses.add(superClass.getSimpleName());
            superClass = superClass.getSuperclass();
        }
        if (parentClasses == null || parentClasses.isEmpty()) return null;
        return ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + StringUtils.join(parentClasses, ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY);
    }

    protected Collection<String> getGoalDescriptions(GoalSelector selector) {
        List<String> descriptions = new ArrayList<>();
        Collection<WrappedGoal> available = selector.getAvailableGoals();
        for (WrappedGoal wrappedGoal : available) {
            Goal goal = wrappedGoal.getGoal();
            String description = goal.toString();
            String parentDescription = getGoalParentDescriptions(goal);
            if (parentDescription != null) {
                description += " " + parentDescription;
            }
            if (wrappedGoal.isRunning()) {
                description = ChatColor.AQUA + description;
            }
            descriptions.add(description);
        }
        return descriptions;
    }


    @Override
    public Collection<String> getGoalDescriptions(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return null;
        }
        return getGoalDescriptions(mob.goalSelector);
    }

    @Override
    public Collection<String> getTargetGoalDescriptions(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return null;
        }
        return getGoalDescriptions(mob.targetSelector);
    }

    protected boolean addGoal(GoalSelector selector, Mob mob, Entity entity, GoalType goalType, ConfigurationSection config) {
        try {
            Goal goal = getGoal(goalType, entity, mob, config);
            if (goal == null) {
                return false;
            }
            int priority = config.getInt("priority", 0);
            selector.addGoal(priority, goal);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error creating goal: " + goalType + " on " + entity.getType(), ex);
            return false;
        }
        return true;
    }


    @Override
    public boolean addGoal(Entity entity, GoalType goalType, ConfigurationSection config) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return addGoal(mob.goalSelector, mob, entity, goalType, config);
    }

    @Override
    public boolean addTargetGoal(Entity entity, GoalType goalType, ConfigurationSection config) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return addGoal(mob.targetSelector, mob, entity, goalType, config);
    }

    private Goal getGoal(GoalType goalType, Entity entity, Mob mob, ConfigurationSection config) {
        final String classType = config.getString("entity_class", "player");
        final double speed = config.getDouble("speed", 1);
        final double sprintSpeed = config.getDouble("sprint_speed", 1);
        final float distance = (float)config.getDouble("distance", 16);
        final boolean doors = config.getBoolean("doors", true);
        final boolean interruptable = config.getBoolean("interruptable", true);
        final boolean see = config.getBoolean("see", true);
        final boolean reach = config.getBoolean("reach", false);
        final float startDistance = (float)config.getDouble("start_distance", 5);
        final float stopDistance = (float)config.getDouble("stop_distance", 1);
        final PathfinderMob pathfinder = mob instanceof PathfinderMob ? (PathfinderMob)mob : null;
        EntityData entityData = platform.getController().getMob(entity);
        int defaultInterval = 1000;
        if (entityData != null) {
            long tickInterval = entityData.getTickInterval();
            if (tickInterval > 0) {
                defaultInterval = (int)tickInterval;
            }
        }
        int interval = config.getInt("interval", defaultInterval);
        // Interval is specified in ms, but needed in ticks
        interval = interval / 50;
        MageController controller = platform.getController();
        Mage mage;
        List<Goal> goals;
        switch (goalType) {
            case AVOID_ENTITY:
                if (pathfinder == null) return null;
                return getAvoidEntityGoal(pathfinder, classType, distance, sprintSpeed, sprintSpeed);
            case BEG:
                if (mob instanceof Wolf) {
                    return new BegGoal((Wolf)mob, distance);
                }
                return null;
            case BREAK_DOOR:
                return new BreakDoorGoal(mob, difficulty -> true);
            case BREATHE_AIR:
                if (pathfinder == null) return null;
                return new BreathAirGoal(pathfinder);
            case BREED:
                if (mob instanceof Animal) {
                    return new BreedGoal((Animal)mob, speed);
                }
                return null;
            case EAT_BLOCK:
                return new EatBlockGoal(mob);
            case FLEE_SUN:
                if (pathfinder == null) return null;
                return new FleeSunGoal(pathfinder, speed);
            case FLOAT:
                return new FloatGoal(mob);
            case FOLLOW_BOAT:
                if (pathfinder == null) return null;
                return new FollowBoatGoal(pathfinder);
            case FOLLOW_FLOCK_LEADER:
                if (mob instanceof AbstractSchoolingFish) {
                    return new FollowFlockLeaderGoal((AbstractSchoolingFish)mob);
                }
                return null;
            case FOLLOW_MOB:
                return new FollowMobGoal(mob, speed, distance, (float)config.getDouble("area_size", 7));
            case FOLLOW_OWNER:
                if (mob instanceof TamableAnimal) {
                    return new FollowOwnerGoal((TamableAnimal)mob, speed, startDistance, stopDistance, config.getBoolean("fly", false));
                }
                // Intentional fall-through
            case MAGIC_FOLLOW_OWNER:
                return new MagicFollowOwnerGoal(platform, mob, entity, speed, startDistance, stopDistance, interval, config);
            case FOLLOW_PARENT:
                if (mob instanceof Animal) {
                    return new FollowParentGoal((Animal)mob, speed);
                }
                return null;
            case GOLEM_RANDOM_STROLL_IN_VILLAGE:
                if (pathfinder == null) return null;
                return new GolemRandomStrollInVillageGoal(pathfinder, speed);
            case INTERACT:
                return getInteractGoal(mob, classType, distance, (float)config.getDouble("probability", 1));
            case LEAP_AT_TARGET:
                return new LeapAtTargetGoal(mob, (float)config.getDouble("y_offset", 0.4));
            case LOOK_AT_PLAYER:
                return getLookAtPlayerGoal(mob, classType, distance, (float)config.getDouble("probability", 1), config.getBoolean("horizontal"));
            case MELEE_ATTACK:
                if (pathfinder == null) return null;
                return new MeleeAttackGoal(pathfinder, speed, config.getBoolean("follow", true));
            case MOVE_BACK_TO_VILLAGE:
                if (pathfinder == null) return null;
                return new MoveBackToVillageGoal(pathfinder, speed, config.getBoolean("check", true));
            case MOVE_THROUGH_VILLAGE:
                if (pathfinder == null) return null;
                return new MoveThroughVillageGoal(pathfinder, speed, config.getBoolean("night", true), (int)distance, (BooleanSupplier) () -> doors);
            case MOVE_TOWARDS_RESTRICTION:
                if (pathfinder == null) return null;
                return new MoveTowardsRestrictionGoal(pathfinder, speed);
            case MOVE_TOWARDS_TARGET:
                if (pathfinder == null) return null;
                return new MoveTowardsTargetGoal(pathfinder, speed, distance);
            case OCELOT_ATTACK:
                return new OcelotAttackGoal(mob);
            case OFFER_FLOWER:
                if (mob instanceof IronGolem) {
                    return new OfferFlowerGoal((IronGolem)mob);
                }
                return null;
            case OPEN_DOOR:
                return new OpenDoorGoal(mob, config.getBoolean("close", false));
            case PANIC:
                if (pathfinder == null) return null;
                return new PanicGoal(pathfinder, speed);
            case RANDOM_LOOK_AROUND:
                return new RandomLookAroundGoal(mob);
            case RANDOM_STROLL:
                if (pathfinder == null) return null;
                return new RandomStrollGoal(pathfinder, speed, interval);
            case RANDOM_SWIMMING:
                if (pathfinder == null) return null;
                return new RandomSwimmingGoal(pathfinder, speed, interval);
            case RESTRICT_SUN:
                if (pathfinder == null) return null;
                return new RestrictSunGoal(pathfinder);
            case RUN_AROUND_LIKE_CRAZY:
                if (mob instanceof Horse) {
                    return new RunAroundLikeCrazyGoal((Horse)mob, speed);
                }
                return null;
            case STROLL_THROUGH_VILLAGE:
                if (pathfinder == null) return null;
                return new StrollThroughVillageGoal(pathfinder, interval);
            case SWELL:
                if (mob instanceof Creeper) {
                    return new SwellGoal((Creeper)mob);
                }
                return null;
            case TEMPT:
                if (pathfinder == null) return null;
                String itemKey = config.getString("item", "EMERALD");
                try {
                    Material material = Material.valueOf(itemKey.toUpperCase());
                    org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(material);
                    ItemUtils itemUtils = platform.getItemUtils();
                    itemStack = itemUtils.makeReal(itemStack);
                    ItemStack nms = (ItemStack)itemUtils.getHandle(itemStack);
                    if (nms == null) {
                        platform.getLogger().warning("Invalid item from material in temp goal: " + itemKey);
                        return null;
                    }
                    boolean scare = config.getBoolean("scare", false);
                    return new TemptGoal(pathfinder, speed, Ingredient.of(nms), scare);
                } catch (Exception ex) {
                    platform.getLogger().warning("Invalid material in temp goal: " + itemKey);
                    return null;
                }
            case TRY_FIND_WATER:
                if (pathfinder == null) return null;
                return new TryFindWaterGoal(pathfinder);
            case WATER_AVOIDING_RANDOM_FLYING:
                if (pathfinder == null) return null;
                return new WaterAvoidingRandomFlyingGoal(pathfinder, speed);
            case WATER_AVOIDING_RANDOM_STROLL:
                if (pathfinder == null) return null;
                return new WaterAvoidingRandomStrollGoal(pathfinder, speed);
            case ZOMBIE_ATTACK:
                if (mob instanceof Zombie) {
                    return new ZombieAttackGoal((Zombie)mob, speed, config.getBoolean("follow", true));
                }
                return null;

            // Target
            case DEFEND_VILLAGE_TARGET:
                if (mob instanceof IronGolem) {
                    return new DefendVillageTargetGoal((IronGolem)mob);
                }
                return null;
            case HURT_BY_TARGET:
                if (pathfinder == null) return null;
                return new HurtByTargetGoal(pathfinder);
            case NEAREST_ATTACKABLE_TARGET:
                return getNearestAttackableTargetGoal(mob, classType, see, reach);
            case OWNER_HURT_BY_TARGET:
                if (mob instanceof TamableAnimal) {
                    return new OwnerHurtByTargetGoal((TamableAnimal)mob);
                }
                // TODO: Custom
                return null;
            case OWNER_HURT_TARGET:
                if (mob instanceof TamableAnimal) {
                    return new OwnerHurtTargetGoal((TamableAnimal)mob);
                }
                // TODO: Custom
                return null;

            // Magic add-ons
            case REQUIREMENT:
            case REQUIREMENTS:
                if (pathfinder == null) return null;
                mage = controller.getMage(entity);
                Collection<Requirement> requirements = controller.getRequirements(config);
                goals = getGoals(entity, mob, config, "magic requirement goal");
                return new RequirementsGoal(mage, goals, interruptable, requirements);
            case GROUP:
                goals = getGoals(entity, mob, config, "magic group goal");
                return new MagicGoal(goals, interruptable);
            case TRIGGER:
                mage = controller.getMage(entity);
                goals = getGoals(entity, mob, config, "magic trigger goal");
                return new TriggerGoal(mage, goals, interruptable, config.getString("trigger", "goal"), interval);
            case IDLE:
                return new IdleGoal();
            default:
                platform.getLogger().warning("Unsupported goal type: " + goalType);
                return null;
        }
    }

    private List<Goal> getGoals(Entity entity, Mob mob, ConfigurationSection config, String logContext) {
        List<Goal> goals = new ArrayList<>();
        List<GoalConfiguration> goalConfigurations = GoalConfiguration.fromList(config, "goals", platform.getLogger(), logContext);
        if (goalConfigurations != null) {
            Collections.sort(goalConfigurations);
            for (GoalConfiguration goalConfig : goalConfigurations) {
                try {
                    Goal goal = getGoal(goalConfig.getGoalType(), entity, mob, goalConfig.getConfiguration());
                    if (goal != null) {
                        goals.add(goal);
                    }
                } catch (Exception ex) {
                    platform.getLogger().log(Level.WARNING, "Error creating goal: " + goalConfig.getGoalType() + " on mob " + entity.getType(), ex);
                }
            }
        }
        if (goals.isEmpty()) {
            goals.add(new IdleGoal());
        }
        return goals;
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

    private Goal getLookAtPlayerGoal(Mob mob, String classType, float distance, float probability, boolean horizontal) {
        Class<? extends LivingEntity> mobClass = getMobClass(classType);
        if (mobClass == null) {
            platform.getLogger().warning("Unsupported entity_class in interact goal: " + classType);
            return null;
        }
        return new LookAtPlayerGoal(mob, mobClass, distance, probability, horizontal);
    }

    private Goal getInteractGoal(Mob mob, String classType, float distance, float probability) {
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

    private Goal getNearestAttackableTargetGoal(Mob mob, String classType, boolean see, boolean reach) {
        switch (classType) {
            case "player":
                return new NearestAttackableTargetGoal<>(mob, Player.class, see, reach);
            case "livingentity":
            case "living_entity":
                return new NearestAttackableTargetGoal<>(mob, LivingEntity.class, see, reach);
            case "monster":
                return new NearestAttackableTargetGoal<>(mob, Monster.class, see, reach);
            case "animal":
                return new NearestAttackableTargetGoal<>(mob, Animal.class, see, reach);
            case "villager":
                return new NearestAttackableTargetGoal<>(mob, Villager.class, see, reach);
            default:
                // TODO: Implement more.. :(
                platform.getLogger().warning("Unsupported entity_class in nearest_attackable_target goal: " + classType);
                return null;
        }
    }

    @Override
    public boolean setPathfinderTarget(Entity entity, Entity target, double speed) {
        if (entity == null || target == null) return false;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)entity).getHandle();
        net.minecraft.world.entity.Entity nmstarget = ((CraftEntity)target).getHandle();
        if (!(nmsEntity instanceof Mob)) {
            return false;
        }

        Mob mob = (Mob)nmsEntity;
        mob.getNavigation().moveTo(nmstarget, speed);
        return true;
    }
}
