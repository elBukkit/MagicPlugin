package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class ThrowItemAction extends BaseSpellAction {
    private double itemSpeed;
    private int ageItems;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        itemSpeed = parameters.getDouble("item_speed");
        ageItems = parameters.getInt("age_items", 5500);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        MaterialAndData material = context.getBrush();
        Block faceBlock = context.getInteractBlock();
        if (faceBlock == null)
        {
            return SpellResult.NO_TARGET;
        }
        Vector velocity = context.getDirection().normalize().multiply(itemSpeed);
        ItemStack itemStack = new ItemStack(material.getMaterial(), 1, material.getData());
        NMSUtils.makeTemporary(itemStack, context.getMessage("removed").replace("$material", material.getName()));
        Item droppedItem = faceBlock.getWorld().dropItem(faceBlock.getLocation(), itemStack);
        droppedItem.setMetadata("temporary", new FixedMetadataValue(context.getController().getPlugin(), true));
        CompatibilityUtils.ageItem(droppedItem, ageItems);
        droppedItem.setVelocity(velocity);
        context.registerForUndo(droppedItem);
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
        parameters.add("item_speed");
        parameters.add("age_items");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("item_speed") || parameterKey.equals("age_items")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }
}
