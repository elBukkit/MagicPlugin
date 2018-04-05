package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.action.builtin.ModifyBlockAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.batch.BlockRecurse;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

@Deprecated
public class RecurseSpell extends BrushSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Block targetBlock = getTargetBlock();

        if (targetBlock == null)
        {
            return SpellResult.NO_TARGET;
        }
        if (!hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (!isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        BlockRecurse blockRecurse = new BlockRecurse();
        int size = parameters.getInt("size", 8);
        size = (int)(mage.getRadiusMultiplier() * size);
        blockRecurse.setMaxRecursion(size);

        ModifyBlockAction action = new ModifyBlockAction();
        action.initialize(this, parameters);
        blockRecurse.addReplaceable(new MaterialAndData(targetBlock));
        Material targetMaterial = targetBlock.getType();

        // A bit hacky, but is very handy!
        if (targetMaterial == Material.WATER)
        {
            for (byte i = 0; i < 9; i++) {
                blockRecurse.addReplaceable(Material.WATER, i);
            }
        }
        else if (targetMaterial == Material.LAVA)
        {
            for (byte i = 0; i < 9; i++) {
                blockRecurse.addReplaceable(Material.LAVA, i);
            }
        }
        else if (targetMaterial == Material.SNOW) {
            for (byte i = 0; i < 8; i++) {
                blockRecurse.addReplaceable(Material.SNOW, i);
            }
        }
        CastContext context = getCurrentCast();
        context.setTargetLocation(targetBlock.getLocation());
        blockRecurse.recurse(new ActionContext(action, parameters), context);
        registerForUndo();
        controller.updateBlock(targetBlock);

        return SpellResult.CAST;
    }
}
