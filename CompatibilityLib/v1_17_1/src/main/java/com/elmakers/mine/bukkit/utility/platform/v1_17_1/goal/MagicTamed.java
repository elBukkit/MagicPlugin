package com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MagicTamed {
    protected final Platform platform;
    protected final Mob mob;
    protected final Entity entity;

    // State
    protected LivingEntity owner;

    public MagicTamed(Platform platform, Mob tamed, Entity entity) {
        this.platform = platform;
        this.mob = tamed;
        this.entity = entity;
    }

    protected void checkOwner() {
        if (owner == null) {
            UUID ownerUUID = platform.getCompatibilityUtils().getOwnerId(entity);
            if (ownerUUID != null) {
                CraftEntity bukkitEntity = (CraftEntity)platform.getCompatibilityUtils().getEntity(ownerUUID);
                if (bukkitEntity.getHandle() instanceof LivingEntity) {
                    owner = (LivingEntity)bukkitEntity.getHandle();
                }
            }
        }
    }

    public boolean isStay() {
        return platform.getEnityMetadataUtils().getBoolean(entity, MagicMetaKeys.STAY);
    }

    public boolean canUse() {
        checkOwner();
        if (owner == null || (owner instanceof Player && ((Player)owner).getGameMode() == GameMode.SPECTATOR)) {
            return false;
        }
        if (isStay()) {
            return false;
        }
        return true;
    }

    public void stop() {
        this.owner = null;
    }

    public LivingEntity getOwner() {
        return owner;
    }
}
