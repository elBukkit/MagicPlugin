package com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal;

import javax.annotation.Nullable;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class Pet extends TamableAnimal {
    private final Mob mob;
    private final LivingEntity owner;

    public Pet(Mob mob, LivingEntity owner) {
        super(EntityType.WOLF, null);
        this.owner = owner;
        this.mob = mob;
        this.level = mob.level;
        this.setBoundingBox(mob.getBoundingBox());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (mob instanceof TamableAnimal) {
            return ((TamableAnimal)mob).getBreedOffspring(serverLevel, ageableMob);
        }
        return null;
    }

    @Override
    public PathNavigation getNavigation() {
        return mob.getNavigation();
    }

    @Override
    public boolean isOrderedToSit() {
        // TODO
        return false;
    }

    @Override
    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public double distanceToSqr(Entity entity) {
        return mob.distanceToSqr(entity);
    }

    @Override
    public float getPathfindingMalus(BlockPathTypes pathtype) {
        return mob.getPathfindingMalus(pathtype);
    }

    @Override
    public void setPathfindingMalus(BlockPathTypes pathtype, float f) {
        mob.setPathfindingMalus(pathtype, f);
    }

    @Override
    public LookControl getLookControl() {
        return mob.getLookControl();
    }

    @Override
    public int getMaxHeadXRot() {
        return mob.getMaxHeadXRot();
    }

    @Override
    public boolean isPassenger() {
        return mob.isPassenger();
    }

    @Override
    public boolean isLeashed() {
        return mob.isLeashed();
    }

    @Override
    public CraftEntity getBukkitEntity() {
        return mob.getBukkitEntity();
    }

    @Override
    public void moveTo(double x, double y, double z, float pitch, float yaw) {
        mob.moveTo(x, y, z, pitch, yaw);
    }

    @Override
    public BlockPos blockPosition() {
        return mob.blockPosition();
    }
}
