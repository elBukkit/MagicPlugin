package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.ParallelCompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class ThrowItemAction extends ParallelCompoundAction {
    private double itemSpeedMin;
    private double itemSpeedMax;
    private int ageItems;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double itemSpeed = parameters.getDouble("speed", 1);
        itemSpeedMin = parameters.getDouble("speed_min", itemSpeed);
        itemSpeedMax = parameters.getDouble("speed_max", itemSpeed);
        ageItems = parameters.getInt("age_items", 5500);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        MaterialAndData material = context.getBrush();
        Location spawnLocation = context.getEyeLocation();
        if (spawnLocation == null)
        {
            return SpellResult.NO_TARGET;
        }
        double itemSpeed = context.getRandom().nextDouble() * (itemSpeedMax - itemSpeedMin) + itemSpeedMin;
        Vector velocity = context.getDirection().normalize().multiply(itemSpeed);
        ItemStack itemStack = new ItemStack(material.getMaterial(), 1, material.getData());
        NMSUtils.makeTemporary(itemStack, context.getMessage("removed").replace("$material", material.getName()));
        Item droppedItem = spawnLocation.getWorld().dropItem(spawnLocation, itemStack);
        droppedItem.setMetadata("temporary", new FixedMetadataValue(context.getController().getPlugin(), true));
        CompatibilityUtils.ageItem(droppedItem, ageItems);
        droppedItem.setVelocity(velocity);
        context.registerForUndo(droppedItem);
        ActionHandler.setActions(droppedItem, actions, context, parameters, "indirect_player_message");
        ActionHandler.setEffects(droppedItem, context, "despawn");
        return SpellResult.CAST;
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
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("speed");
        parameters.add("speed_min");
        parameters.add("speed_max");
        parameters.add("age_items");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("speed") || parameterKey.equals("age_items")
        || parameterKey.equals("speed_max") || parameterKey.equals("speed_min"))
        {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }
}
