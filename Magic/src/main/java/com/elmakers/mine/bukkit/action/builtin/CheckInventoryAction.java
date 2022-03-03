package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
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
    private boolean targetCaster;
    private boolean targetBlock;
    private boolean materialOnly;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        targetBlock = parameters.getBoolean("target_block", false);
        targetCaster = parameters.getBoolean("target_caster", false);
        materialOnly = parameters.getBoolean("material_only", false);
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
    protected boolean checkBlock(CastContext context) {
        Block targetBlock = context.getTargetBlock();
        BlockState state = targetBlock == null ? null : targetBlock.getState();
        if (state == null || !(state instanceof InventoryHolder)) {
            return false;
        }
        InventoryHolder holder = (InventoryHolder)state;
        if (slot != null) {
            int slotNumber = slot.getSlot();
            if (slotNumber == -1) {
                context.getLogger().warning("Invalid slot for CheckInventory action: " + slot);
                return false;
            }
            ItemStack item = holder.getInventory().getItem(slotNumber);
            return checkItem(item);
        }
        if (this.item == null) {
            context.getLogger().warning("CheckInventory needs an item or slot to check for a container");
            return false;
        }
        if (materialOnly) {
            return holder.getInventory().contains(this.item.getType());
        }
        return holder.getInventory().contains(this.item);
    }

    protected boolean checkItem(ItemStack checkItemStack) {
        boolean defaultResult = false;
        if (this.item != null) {
            if (CompatibilityLib.getItemUtils().isEmpty((checkItemStack))) {
                return CompatibilityLib.getItemUtils().isEmpty(this.item);
            }
            if (materialOnly) {
                return checkItemStack.getType() == this.item.getType() && checkItemStack.getAmount() >= this.item.getAmount();
            }
            return checkItemStack.isSimilar(this.item) && checkItemStack.getAmount() >= this.item.getAmount();
        }
        ItemMeta meta = checkItemStack == null ? null : checkItemStack.getItemMeta();
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

    @Override
    protected boolean isAllowed(CastContext context) {
        Mage mage;

        if (targetBlock) {
            return checkBlock(context);
        }

        if (targetCaster) {
            mage = context.getMage();
        } else {
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null || !(targetEntity instanceof Player)) return false;
            mage = context.getController().getMage(targetEntity);
        }
        if (slot != null) {
            int slotNumber = slot.getSlot(mage);
            if (slotNumber == -1) {
                context.getLogger().warning("Invalid slot for CheckInventory action: " + slot);
                return false;
            }
            ItemStack item = mage.getItem(slotNumber);
            return checkItem(item);
        }
        return item != null && mage.hasItem(item);
    }

    @Override
    public boolean requiresTarget() {
        return !targetCaster;
    }

    @Override
    public boolean requiresTargetEntity() {
        return !targetCaster && !targetBlock;
    }
}
