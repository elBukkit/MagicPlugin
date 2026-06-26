package com.elmakers.mine.bukkit.utility.platform.base_v26_2;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;

import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;

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

    @Override
    public Item dropItem(Location loc, org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ServerLevel level = ((CraftWorld)loc.getWorld()).getHandle();
        ItemEntity entity = new ItemEntity(level, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
        entity.pickupDelay = 10;
        return (Item)entity.getBukkitEntity();
    }

    @Override
    public Entity spawnWithData(EntityType entityType, Location location, Object data) {
        if (entityType == null) return null;
        net.minecraft.world.entity.EntityType<?> nmsType = CraftEntityType.bukkitToMinecraft(entityType);
        ServerLevel nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        net.minecraft.world.entity.Entity nmsEntity = net.minecraft.world.entity.EntityType.loadEntityRecursive(nmsType, (CompoundTag)data, nmsWorld, EntitySpawnReason.COMMAND, (e) -> {
            e.setXRot(location.getPitch());
            e.setYRot(location.getYaw());
            e.setPos(location.getX(), location.getY(), location.getZ());
            return e;
        });
        if (nmsEntity == null) return null;
        return nmsEntity.getBukkitEntity();
    }
}
