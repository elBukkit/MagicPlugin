package com.elmakers.mine.bukkit.utility.platform.base_v1_21_4;

import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtilsBase;

public abstract class Modern2CompatibilityUtils extends CompatibilityUtilsBase {

    public Modern2CompatibilityUtils(Platform platform) {
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
}
