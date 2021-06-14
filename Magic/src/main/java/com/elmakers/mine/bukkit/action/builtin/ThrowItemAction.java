package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.metadata.EntityMetadataUtils;

public class ThrowItemAction extends BaseProjectileAction {
    private double itemSpeedMin;
    private double itemSpeedMax;
    private int ageItems;
    private boolean unbreakable;
    private SourceLocation sourceLocation;
    private ItemData item;
    private boolean throwWand;
    private boolean temporary;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double itemSpeed = parameters.getDouble("speed", 1);
        itemSpeedMin = parameters.getDouble("speed_min", itemSpeed);
        itemSpeedMax = parameters.getDouble("speed_max", itemSpeed);
        ageItems = parameters.getInt("age_items", 5500);
        unbreakable = parameters.getBoolean("unbreakable", false);
        throwWand = parameters.getBoolean("throw_wand", false);
        temporary = parameters.getBoolean("temporary", true);
        sourceLocation = new SourceLocation(parameters);

        String itemName = parameters.getString("item");
        if (itemName != null && !itemName.isEmpty()) {
            item = context.getController().getOrCreateItem(itemName);
        }
    }

    @Override
    public SpellResult start(CastContext context)
    {
        Location spawnLocation = sourceLocation.getLocation(context);
        if (spawnLocation == null)
        {
            return SpellResult.NO_TARGET;
        }
        ItemStack itemStack = null;
        if (throwWand) {
            Wand wand = context.getWand();
            if (wand == null) {
                return SpellResult.NO_TARGET;
            }
            wand.deactivate();

            itemStack = wand.getItem();
            if (CompatibilityLib.getItemUtils().isEmpty(itemStack))
            {
                return SpellResult.FAIL;
            }
            int slotNumber = wand.getHeldSlot();
            Player player = context.getMage().getPlayer();
            if (player != null) {
                player.getInventory().setItem(slotNumber, new ItemStack(Material.AIR));
            }
        } else if (item != null) {
            itemStack = item.getItemStack(1);
        } else {
            MaterialAndData material = context.getBrush();
            if (material != null) {
                itemStack = new ItemStack(material.getMaterial(), 1, material.getData());
            }
        }

        if (itemStack == null)
        {
            return SpellResult.NO_TARGET;
        }
        double itemSpeed = context.getRandom().nextDouble() * (itemSpeedMax - itemSpeedMin) + itemSpeedMin;
        Vector velocity = spawnLocation.getDirection().normalize().multiply(itemSpeed);
        if (unbreakable) {
            itemStack = CompatibilityLib.getItemUtils().makeReal(itemStack);
            CompatibilityLib.getItemUtils().makeUnbreakable(itemStack);
        }
        Item droppedItem = null;
        try {
            droppedItem = spawnLocation.getWorld().dropItem(spawnLocation, itemStack);
        } catch (Exception ignored) {

        }
        if (droppedItem == null) {
            context.getMage().sendDebugMessage("Failed to spawn item of type " + itemStack.getType());
            return SpellResult.FAIL;
        }
        if (temporary) {
            EntityMetadataUtils.instance().setBoolean(droppedItem, MagicMetaKeys.TEMPORARY, true);

            // Make item temporary after spawning, otherwise the spawn will be cancelled
            itemStack = droppedItem.getItemStack();
            if (itemStack != null) {
                String removedMessage = context.getMessage("removed");
                if (removedMessage != null) {
                    String name = context.getController().describeItem(itemStack);
                    if (name == null) {
                        name = "";
                    }
                    removedMessage = removedMessage.replace("$material", name);
                }
                CompatibilityLib.getItemUtils().makeTemporary(itemStack, removedMessage);
                droppedItem.setItemStack(itemStack);
            }
        }
        if (ageItems > 0) {
            CompatibilityLib.getCompatibilityUtils().ageItem(droppedItem, ageItems);
        }
        SafetyUtils.setVelocity(droppedItem, velocity);

        track(context, droppedItem);
        return checkTracking(context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean usesBrush() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("speed");
        parameters.add("speed_min");
        parameters.add("speed_max");
        parameters.add("age_items");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("speed") || parameterKey.equals("age_items")
        || parameterKey.equals("speed_max") || parameterKey.equals("speed_min"))
        {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
