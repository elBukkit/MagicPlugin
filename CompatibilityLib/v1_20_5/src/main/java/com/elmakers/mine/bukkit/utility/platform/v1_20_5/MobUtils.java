package com.elmakers.mine.bukkit.utility.platform.v1_20_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.mob.GoalConfiguration;
import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.base.MobUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.IdleGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicCheckOwnerGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicFindOwnerGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicFollowMobGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicFollowOwnerGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicOwnerHurtByTargetGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicOwnerHurtTargetGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.MagicPanicGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.RequirementsGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.SpinGoal;
import com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal.TriggerGoal;

import net.minecraft.world.entity.GlowSquid;
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
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
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
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
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
        mob.goalSelector.getAvailableGoals().clear();
        return true;
    }

    @Override
    public boolean removeTargetGoals(Entity entity) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        mob.targetSelector.getAvailableGoals().clear();
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
            } else {
                description = ChatColor.GOLD + description;
            }
            descriptions.add(ChatColor.BLUE + Integer.toString(wrappedGoal.getPriority()) + ChatColor.DARK_GRAY + ": " + description);
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

    protected boolean addGoal(GoalSelector selector, Mob mob, Entity entity, GoalType goalType, int priority, ConfigurationSection config) {
        try {
            Goal goal = getGoal(goalType, entity, mob, config);
            if (goal == null) {
                return false;
            }
            selector.addGoal(priority, goal);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error creating goal: " + goalType + " on " + entity.getType(), ex);
            return false;
        }
        return true;
    }


    @Override
    public boolean addGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return addGoal(mob.goalSelector, mob, entity, goalType, priority, config);
    }

    @Override
    public boolean addTargetGoal(Entity entity, GoalType goalType, int priority, ConfigurationSection config) {
        Mob mob = getMob(entity);
        if (mob == null) {
            return false;
        }
        return addGoal(mob.targetSelector, mob, entity, goalType, priority, config);
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
        final float radius = (float)config.getDouble("radius", 16);
        final PathfinderMob pathfinder = mob instanceof PathfinderMob ? (PathfinderMob)mob : null;
        int interval = config.getInt("interval", 1000);
        // Interval is specified in ms, but needed in ticks
        interval = interval / 50;
        MageController controller = platform.getController();
        Mage mage;
        List<Goal> goals;
        switch (goalType) {
            case AVOID_ENTITY:
                if (pathfinder == null) return null;
                return new AvoidEntityGoal<>(pathfinder, getMobClass(classType), distance, sprintSpeed, sprintSpeed);
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
                return new FollowMobGoal(mob, speed, distance, radius);
            case FOLLOW_OWNER:
                if (mob instanceof TamableAnimal) {
                    return new FollowOwnerGoal((TamableAnimal)mob, speed, startDistance, stopDistance, config.getBoolean("fly", false));
                }
                // Intentional fall-through
            case MAGIC_FOLLOW_OWNER:
                return new MagicFollowOwnerGoal(platform, mob, speed, startDistance, stopDistance, interval, config);
            case FOLLOW_PARENT:
                if (mob instanceof Animal) {
                    return new FollowParentGoal((Animal)mob, speed);
                }
                return null;
            case GOLEM_RANDOM_STROLL_IN_VILLAGE:
                if (pathfinder == null) return null;
                return new GolemRandomStrollInVillageGoal(pathfinder, speed);
            case INTERACT:
                return new InteractGoal(mob, getMobClass(classType), distance, (float)config.getDouble("probability", 1));
            case LAND_ON_OWNERS_SHOULDER:
                if (mob instanceof ShoulderRidingEntity) {
                    return new LandOnOwnersShoulderGoal((ShoulderRidingEntity)mob);
                }
                return null;
            case LEAP_AT_TARGET:
                return new LeapAtTargetGoal(mob, (float)config.getDouble("y_offset", 0.4));
            case LOOK_AT_PLAYER:
                return new LookAtPlayerGoal(mob, getMobClass(classType), distance, (float)config.getDouble("probability", 1), config.getBoolean("horizontal"));
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
            case PANIC_FIRE:
                if (pathfinder == null) return null;
                return new PanicGoal(pathfinder, speed);
            case PANIC:
            case MAGIC_PANIC:
                if (pathfinder == null) return null;
                return new MagicPanicGoal(pathfinder, speed, config.getInt("panic", 3000), config.getInt("calm", 5000), interruptable);
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
                return new NearestAttackableTargetGoal<>(mob, getMobClass(classType), see, reach);
            case OWNER_HURT_BY_TARGET:
                if (mob instanceof TamableAnimal) {
                    return new OwnerHurtByTargetGoal((TamableAnimal)mob);
                }
                // Intentional fall-through
            case MAGIC_OWNER_HURT_BY_TARGET:
                return new MagicOwnerHurtByTargetGoal(platform, mob, entity, see, reach);
            case OWNER_HURT_TARGET:
                if (mob instanceof TamableAnimal) {
                    return new OwnerHurtTargetGoal((TamableAnimal)mob);
                }
                // Intentional fall-through
            case MAGIC_OWNER_HURT_TARGET:
                return new MagicOwnerHurtTargetGoal(platform, mob, entity, see, reach);

            // Magic add-ons
            case FOLLOW_ENTITY:
            case MAGIC_FOLLOW_MOB:
                Class<? extends LivingEntity> mobClass = getMobClass(classType);
                if (mobClass == null) {
                    platform.getLogger().warning("Unsupported entity_class in magic_follow_mob goal: " + classType);
                    return null;
                }
                return new MagicFollowMobGoal(mob, speed, radius, distance, interval, mobClass);
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
            case FIND_OWNER:
                return new MagicFindOwnerGoal(platform, mob, radius, getMobClass(classType));
            case CHECK_OWNER:
                return new MagicCheckOwnerGoal(platform, mob);
            case IDLE:
                return new IdleGoal();
            case SPIN:
                return new SpinGoal(mob, (float)config.getDouble("degrees", 10));
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
            case "ambient_creature":
            case "ambientcreature":

            // Ambient
            case "ambient":
                return AmbientCreature.class;
            case "bat":
                return Bat.class;

            // Axolotl
            case "axolotl":
                return Axolotl.class;

            // Goat
            case "goat":
                return Goat.class;

            // Horse
            case "abstractchestedhorse":
            case "abstract_chested_horse":
            case "chestedhorse":
            case "chested_horse":
                return AbstractChestedHorse.class;
            case "abstract_horse":
            case "abstracthorse":
            case "any_horse":
                return AbstractHorse.class;
            case "donkey":
                return Donkey.class;
            case "horse":
                return Horse.class;
            case "llama":
                return Llama.class;
            case "mule":
                return Mule.class;
            case "skeleton_horse":
            case "skeletonhorse":
                return Skeleton.class;
            case "trader_llama":
            case "traderllama":
                return TraderLlama.class;
            case "zombiehorse":
            case "zombie_horse":
                return ZombieHorse.class;

            // Animal
            case "abstractfish":
            case "abstract_fish":
            case "any_fish":
            case "fish":
                return AbstractFish.class;
            case "abstractgolem":
            case "abstract_golem":
            case "golem":
                return AbstractGolem.class;
            case "abstractschoolingfish":
            case "abstract_schooling_fish":
            case "schooling_fish":
                return AbstractSchoolingFish.class;
            case "animal":
                return Animal.class;
            case "bee":
                return Bee.class;
            case "cat":
                return Cat.class;
            case "chicken":
                return Chicken.class;
            case "cod":
                return Cod.class;
            case "cow":
                return Cow.class;
            case "dolphin":
                return Dolphin.class;
            case "fox":
                return Fox.class;
            case "iron_golem":
            case "irongolem":
                return IronGolem.class;
            case "mushroomcow":
            case "mushroom_cow":
            case "mooshroom":
                return MushroomCow.class;
            case "ocelot":
                return Ocelot.class;
            case "panda":
                return Panda.class;
            case "parrot":
                return Parrot.class;
            case "pig":
                return Pig.class;
            case "polarbear":
            case "polar_bear":
                return PolarBear.class;
            case "pufferfish":
                return Pufferfish.class;
            case "rabbit":
                return Rabbit.class;
            case "salmon":
                return Salmon.class;
            case "sheep":
                return Sheep.class;
            case "snowgolem":
            case "snow_golem":
            case "snowman":
                return SnowGolem.class;
            case "squid":
                return Squid.class;
            case "tropicalfish":
            case "tropical_fish":
                return TropicalFish.class;
            case "turle":
            case "sea_turtle":
                return Turtle.class;
            case "wolf":
                return Wolf.class;

            // Boss
            case "dragon":
            case "enderdragon":
            case "ender_dragon":
                return EnderDragon.class;
            case "wither":
            case "wither_boss":
            case "witherboss":
                return WitherBoss.class;

            // Decoration
            case "armorstand":
            case "armor_stand":
                return ArmorStand.class;

            // Hoglin
            case "hoglin":
                return Hoglin.class;

            // Piglin
            case "piglin":
                return Piglin.class;
            case "piglinbrute":
            case "piglin_brute":
                return PiglinBrute.class;

            // Monster
            case "abstractillager":
            case "abstract_illager":
            case "illager":
                return AbstractIllager.class;
            case "abstractskeleton":
            case "abstract_skeleton":
            case "any_skeleton":
                return AbstractSkeleton.class;
            case "blaze":
                return Blaze.class;
            case "cavespider":
            case "cave_spider":
                return CaveSpider.class;
            case "creeper":
                return Creeper.class;
            case "drowned":
                return Drowned.class;
            case "elderguardian":
            case "elder_guardian":
                return ElderGuardian.class;
            case "enderman":
                return EnderMan.class;
            case "endermite":
                return Endermite.class;
            case "evoker":
                return Evoker.class;
            case "ghast":
                return Ghast.class;
            case "giant":
                return Giant.class;
            case "guardian":
                return Guardian.class;
            case "husk":
                return Husk.class;
            case "illusioner":
                return Illusioner.class;
            case "magmacube":
            case "magma_cube":
                return MagmaCube.class;
            case "monster":
                return Monster.class;
            case "phantom":
                return Phantom.class;
            case "pillager":
                return Pillager.class;
            case "ravager":
                return Ravager.class;
            case "shulker":
                return Shulker.class;
            case "silverfish":
                return Silverfish.class;
            case "skeleton":
                return Skeleton.class;
            case "slime":
                return Slime.class;
            case "spellcasterillager":
            case "spellcaster_illager":
            case "spellcaster":
                return SpellcasterIllager.class;
            case "spider":
                return Spider.class;
            case "stray":
                return Stray.class;
            case "strider":
                return Strider.class;
            case "vex":
                return Vex.class;
            case "vindicator":
                return Vindicator.class;
            case "witch":
                return Witch.class;
            case "witherskeleton":
            case "wither_skeleton":
                return WitherSkeleton.class;
            case "zoglin":
                return Zoglin.class;
            case "zombie":
                return Zombie.class;
            case "zombievillager":
            case "zombie_villager":
                return ZombieVillager.class;
            case "zombifiedpiglin":
            case "zombified_piglin":
                return ZombifiedPiglin.class;

            // NPC
            case "villager":
                return Villager.class;
            case "wanderingtrader":
            case "wandering_trader":
                return WanderingTrader.class;

            // Player
            case "player":
                return Player.class;

            // Base
            // Why are you here?
            case "glowsquid":
                return GlowSquid.class;
            case "mob":
                return Mob.class;
            case "livingentity":
            case "living_entity":
                return LivingEntity.class;

            default:
                platform.getLogger().warning("Invalid entity_class in goal config: " + classType);
                return LivingEntity.class;
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
