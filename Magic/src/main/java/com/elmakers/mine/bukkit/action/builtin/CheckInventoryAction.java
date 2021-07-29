package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.item.InventorySlot;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class CheckInventoryAction extends CheckAction {
    private ItemStack item;
    private InventorySlot slot;
    private Collection<Enchantment> allowedEnchantments;
    private Collection<Enchantment> blockedEnchantments;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        String itemKey = parameters.getString("item");
        if (itemKey != null && !itemKey.isEmpty()) {
            item = context.getController().createItem(itemKey);
            if (item == null) {
                context.getLogger().warning("Invalid item: " + itemKey);
            }
        }

        String slotName = parameters.getString("slot");
        if (slotName != null && !slotName.isEmpty()) {
            slot = InventorySlot.parse(slotName);
            if (slot == null) {
                context.getLogger().warning("Invalid slot in CheckInventory action: " + slotName);
            }
        }

        allowedEnchantments = parseEnchantmentList(context, parameters, "allowed_enchantments");
        blockedEnchantments = parseEnchantmentList(context, parameters, "blocked_enchantments");
    }

    @Nullable
    private Collection<Enchantment> parseEnchantmentList(CastContext context, ConfigurationSection parameters, String key) {
        Collection<Enchantment> enchantments = null;
        List<String> keys = parameters.getStringList(key);
        if (keys != null && !keys.isEmpty()) {
            enchantments = new ArrayList<>();
            CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
            for (String enchantmentKey : keys) {
                Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantmentKey);
                if (enchantment != null) {
                    enchantments.add(enchantment);
                } else {
                    context.getLogger().warning("Invalid enchantment in CheckInventory action " + key + ": " + enchantmentKey);
                }
            }
        }
        return enchantments;
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null || !(targetEntity instanceof Player)) return false;
        Mage mage = context.getController().getMage(targetEntity);
        if (slot != null) {
            boolean defaultResult = false;
            int slotNumber = slot.getSlot(mage);
            if (slotNumber == -1) {
                context.getLogger().warning("Invalid slot for CheckInventory action: " + slot);
                return false;
            }
            ItemStack item = mage.getItem(slotNumber);
            if (this.item != null && Objects.equals(item, this.item)) {
                return true;
            }
            ItemMeta meta = item == null ? null : item.getItemMeta();
            if (blockedEnchantments != null) {
                defaultResult = true;
                if (meta != null) {
                    for (Enchantment enchantment : blockedEnchantments) {
                        if (meta.hasEnchant(enchantment)) {
                            return false;
                        }
                    }
                }
            }
            if (allowedEnchantments != null) {
                defaultResult = false;
                if (meta == null) {
                    return false;
                }
                for (Enchantment enchantment : allowedEnchantments) {
                    if (meta.hasEnchant(enchantment)) {
                        return true;
                    }
                }
            }

            return defaultResult;
        }
        return item != null && mage.hasItem(item);
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
