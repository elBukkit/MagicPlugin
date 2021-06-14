package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.item.InventorySlot;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

public class ColorItemAction extends BaseSpellAction {
    private List<InventorySlot> slots;
    private ColorHD color;
    private boolean useWand;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        List<String> slots = ConfigurationUtils.getStringList(parameters, "slots");
        String slot = parameters.getString("slot");
        if (slot != null && !slot.isEmpty()) {
            if (slots == null) {
                slots = new ArrayList<>();
            }
            slots.add(slot);
        }
        if (slots != null) {
            this.slots = new ArrayList<>();
            for (String slotKey : slots) {
                InventorySlot inventorySlot = InventorySlot.parse(slotKey);
                if (inventorySlot != null) {
                    this.slots.add(inventorySlot);
                } else {
                    context.getLogger().warning("Invalid slot in ColorItem action: " + slotKey);
                }
            }
        }

        ConfigurationSection colorSection = parameters.getConfigurationSection("color");
        if (colorSection != null) {
            color = new ColorHD(colorSection);
        } else {
            color = new ColorHD(parameters.getString("color"));
        }
        useWand = parameters.getBoolean("color_wand");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        boolean colored = false;
        if (useWand) {
            Wand wand = context.getWand();
            if (wand != null) {
                if (color(context, wand.getItem())) {
                    colored = true;
                }
            }
        }
        if (slots == null || slots.isEmpty()) {
            return colored ? SpellResult.CAST : SpellResult.NO_TARGET;
        }
        Entity entity = context.getTargetEntity();

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            EntityEquipment equipment = li.getEquipment();
            if (equipment == null) {
                return SpellResult.NO_TARGET;
            }
            for (InventorySlot slot : slots) {
                ItemStack slotItem = slot.getItem(equipment);
                if (color(context, slotItem)) {
                    colored = true;
                    slot.setItem(equipment, slotItem);
                }
            }
            return colored ? SpellResult.CAST : SpellResult.NO_TARGET;
        }

        if (entity == null || !(entity instanceof Item)) {
            return SpellResult.NO_TARGET;
        }
        Item item = (Item)entity;
        ItemStack itemStack = item.getItemStack();
        if (color(context, itemStack)) {
            colored = true;
        }
        return colored ? SpellResult.CAST : SpellResult.NO_TARGET;
    }

    protected boolean color(CastContext context, ItemStack itemStack) {
        if (ItemUtils.isEmpty(itemStack)) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (!(meta instanceof LeatherArmorMeta)) {
            return false;
        }
        Wand wand = context.getController().getIfWand(itemStack);
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta)meta;
        leatherMeta.setColor(color.getColor());
        itemStack.setItemMeta(leatherMeta);
        if (wand != null) {
            wand.setIcon(new MaterialAndData(itemStack));
        }
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
