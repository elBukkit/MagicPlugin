package com.elmakers.mine.bukkit.utility.platform.modern2;

import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernCompatibilityUtils;

public abstract class Modern2CompatibilityUtils extends ModernCompatibilityUtils {

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
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            AttributeModifier.Operation operation;
            try {
                operation = AttributeModifier.Operation.values()[attributeOperation];
            } catch (Throwable ex) {
                platform.getLogger().warning("[Magic] invalid attribute operation ordinal: " + attributeOperation);
                return false;
            }
            AttributeModifier modifier;

            NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), "modifier");
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.ANY;
            if (slot != null && !slot.isEmpty()) {
                try {
                    if (slot.equalsIgnoreCase("mainhand")) {
                        equipmentSlotGroup = EquipmentSlotGroup.MAINHAND;
                    } else if (slot.equalsIgnoreCase("offhand")) {
                        equipmentSlotGroup = EquipmentSlotGroup.OFFHAND;
                    } else {
                        equipmentSlotGroup = EquipmentSlotGroup.getByName(slot.toUpperCase());
                    }
                } catch (Throwable ex) {
                    platform.getLogger().warning("[Magic] invalid attribute slot: " + slot);
                    return false;
                }
            }
            modifier = new AttributeModifier(namespacedKey, value, operation, equipmentSlotGroup);
            meta.addAttributeModifier(attribute, modifier);
            item.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
