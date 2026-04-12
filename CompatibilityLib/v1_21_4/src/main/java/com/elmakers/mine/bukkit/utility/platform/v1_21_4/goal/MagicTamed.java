package com.elmakers.mine.bukkit.utility.platform.v1_21_4.goal;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MagicTamed {
    protected final Platform platform;
    protected final Mob mob;

    // State
    protected LivingEntity owner;

    public MagicTamed(Platform platform, Mob tamed) {
        this.platform = platform;
        this.mob = tamed;
    }

    protected void checkOwner() {
        if (owner == null) {
            UUID ownerUUID = platform.getCompatibilityUtils().getOwnerId(mob.getBukkitEntity());
            if (ownerUUID != null) {
                CraftEntity bukkitEntity = (CraftEntity)platform.getCompatibilityUtils().getEntity(ownerUUID);
                if (bukkitEntity != null && bukkitEntity.getHandle() instanceof LivingEntity) {
                    owner = (LivingEntity)bukkitEntity.getHandle();
                }
            }
        }
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        platform.getCompatibilityUtils().setOwner(mob.getBukkitEntity(), owner == null ? null : owner.getBukkitEntity());
    }

    public boolean isStay() {
        return platform.getEnityMetadataUtils().getBoolean(mob.getBukkitEntity(), MagicMetaKeys.STAY);
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
