package com.elmakers.mine.bukkit.utility.platform.base_v1_21_4;

import java.util.UUID;

import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtilsBase;

public abstract class CompatibilityUtilsBase_v1_21_4 extends CompatibilityUtilsBase {

    public CompatibilityUtilsBase_v1_21_4(Platform platform) {
        super(platform);
    }

    @Override
    public boolean isJumping(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return player.getCurrentInput().isJump();
        }
        return false;
    }

    @Override
    public float getForwardMovement(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getCurrentInput().isForward()) {
                // Forward + backwards cancels out
                if (player.getCurrentInput().isBackward()) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (player.getCurrentInput().isBackward()) {
                return -1;
            }
        }
        return 0.0f;
    }

    @Override
    public float getStrafeMovement(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getCurrentInput().isRight()) {
                // Left + right cancels out
                if (player.getCurrentInput().isLeft()) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (player.getCurrentInput().isLeft()) {
                return 1;
            }
        }
        return 0.0f;
    }

    @Override
    protected AttributeModifier createAttributeModifier(UUID attributeUUID, double value, AttributeModifier.Operation operation, EquipmentSlotGroup equipmentSlotGroup) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), "modifier");
        return new AttributeModifier(namespacedKey, value, operation, equipmentSlotGroup);
    }

    @Override
    public Object getTileEntity(Location location) {
        throw new UnsupportedOperationException("The getTileEntity method is no longer supported");
    }

    @Override
    public BlockState getTileEntityData(Location location) {
        if (location == null) return null;
        return location.getBlock().getState();
    }

    @Override
    public void setTileEntityData(Location location, Object data) {
        if (location == null || data == null || !(data instanceof BlockState)) return;
        BlockState blockState = (BlockState)data;
        blockState.copy(location);
        blockState.update();
    }

    @Override
    public void clearItems(Location location) {
        if (location == null) return;

        // Block-specific behaviors
        Block block = location.getBlock();
        BlockState blockState = block.getState();
        if (blockState instanceof Lootable) {
            Lootable lootable = (Lootable)blockState;
            lootable.setLootTable(null);
            blockState.update();
        }
        if (blockState instanceof Lectern) {
            Lectern lectern = (Lectern)blockState;
            lectern.getInventory().setItem(0, new ItemStack(Material.AIR));
            blockState.update();
        }
        if (blockState instanceof Jukebox) {
            ((Jukebox) blockState).setRecord(null);
            blockState.update();
        }
        if (blockState instanceof Container) {
            ((Container) blockState).getInventory().clear();
            blockState.update();
        }
    }

    @Override
    public boolean isDestructive(EntityExplodeEvent explosion) {
        ExplosionResult result = explosion.getExplosionResult();
        return result == ExplosionResult.DESTROY || result == ExplosionResult.DESTROY_WITH_DECAY;
    }
}
