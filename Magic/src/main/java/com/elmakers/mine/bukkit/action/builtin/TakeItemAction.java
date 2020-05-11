package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

public class TakeItemAction extends BaseSpellAction
{
    private String displayName;
    private MaterialAndData itemType;
    private boolean giveToCaster;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        displayName = parameters.getString("display_name", null);
        if (displayName != null) {
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        }
        giveToCaster = true;
        String itemKey = parameters.getString("item", "");
        if (!itemKey.isEmpty()) {
            giveToCaster = false;
            itemType = new MaterialAndData(itemKey);
        }
        giveToCaster = parameters.getBoolean("give_to_caster", giveToCaster);
    }

    private boolean checkItem(ItemStack item) {
        if (displayName != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals(displayName)) {
                return false;
            }
        }
        if (itemType != null) {
            MaterialAndData check = new MaterialAndData(item);
            if (!itemType.equals(check)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }

        ItemStack item = null;
        if (target instanceof Player) {
            Player targetPlayer = (Player)target;
            PlayerInventory playerInventory = targetPlayer.getInventory();
            for (int i = 0; i < playerInventory.getSize(); i++) {
                ItemStack inventoryItem = playerInventory.getItem(i);
                if (InventoryUtils.isEmpty(inventoryItem)) continue;
                if (checkItem(inventoryItem)) {
                    item = inventoryItem;
                    playerInventory.setItem(i, null);
                    break;
                }
            }
        } else if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)target;
            EntityEquipment equipment = livingEntity.getEquipment();
            ItemStack equipmentItem = equipment.getItemInMainHand();
            if (checkItem(equipmentItem)) {
                context.registerModified(livingEntity);
                equipment.setItemInMainHand(null);
                item = equipmentItem;
            }
            equipmentItem = equipment.getItemInOffHand();
            if (item == null && checkItem(equipmentItem)) {
                context.registerModified(livingEntity);
                equipment.setItemInOffHand(null);
                item = equipmentItem;
            }
            if (item == null) {
                ItemStack[] armor = equipment.getArmorContents();
                for (int i = 0; i < armor.length; i++) {
                    if (checkItem(armor[i])) {
                       context.registerModified(livingEntity);
                       item = armor[i];
                       armor[i] = null;
                       equipment.setArmorContents(armor);
                       break;
                    }
                }
            }
        } else if (target instanceof Item) {
            Item itemEntity = (Item)target;
            ItemStack itemStack = itemEntity.getItemStack();
            if (checkItem(itemStack)) {
                item = itemStack;
                context.registerModified(itemEntity);
                itemEntity.remove();
            }
        }

        if (InventoryUtils.isEmpty(item)) {
            return SpellResult.NO_TARGET;
        }

        if (giveToCaster) {
            context.getMage().giveItem(item);
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
