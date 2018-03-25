package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class RepairItemAction extends BaseSpellAction
{
    private boolean armor;
    private boolean heldItem;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        armor = parameters.getBoolean("armor", false);
        heldItem = parameters.getBoolean("held_item", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();

        if (entity instanceof LivingEntity && (armor || heldItem))
        {
            boolean repaired = false;
            LivingEntity li = (LivingEntity)entity;
            EntityEquipment equipment = li.getEquipment();
            if (equipment == null)
            {
                return SpellResult.NO_TARGET;
            }
            if (armor)
            {
                ItemStack item = equipment.getHelmet();
                repaired = repair(context, item) || repaired;
                item = equipment.getChestplate();
                repaired = repair(context, item) || repaired;
                item = equipment.getLeggings();
                repaired = repair(context, item) || repaired;
                item = equipment.getBoots();
                repaired = repair(context, item) || repaired;
            }
            if (heldItem)
            {
                ItemStack item = equipment.getItemInMainHand();
                repaired = repair(context, item) || repaired;
            }
            return repaired ? SpellResult.CAST : SpellResult.NO_TARGET;
        }

        if (entity == null || !(entity instanceof Item)) {
            return SpellResult.NO_TARGET;
        }
        Item item = (Item)entity;
        ItemStack itemStack = item.getItemStack();
        return repair(context, itemStack) ? SpellResult.CAST : SpellResult.NO_TARGET;
    }

    protected boolean repair(CastContext context, ItemStack itemStack)
    {
        if (itemStack == null || itemStack.getType() == Material.AIR || context.getController().getAPI().isWand(itemStack))
        {
            return false;
        }
        short maxDurability = itemStack.getType().getMaxDurability();
        if (maxDurability <= 0 || itemStack.getDurability() <= 0)
        {
            return false;
        }
        itemStack.setDurability((short)0);
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
