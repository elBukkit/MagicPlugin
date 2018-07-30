package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class ExplosionAction extends BaseSpellAction {

    protected int size;
    protected boolean useFire;
    protected boolean breakBlocks;

    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
        if (breakBlocks && !context.hasBreakPermission(block))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (useFire && !context.hasBuildPermission(block))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        Entity entity = context.getEntity();
        Location location = block.getLocation();

        // Make sure undo info on exploding blocks gets attached to this cast.
        UndoList currentList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
        com.elmakers.mine.bukkit.block.UndoList.setUndoList(context.getPlugin(), entity, context.getUndoList());
        NMSUtils.createExplosion(entity, location.getWorld(), location.getX(), location.getY(), location.getZ(), size, useFire, breakBlocks);
        com.elmakers.mine.bukkit.block.UndoList.setUndoList(context.getPlugin(), entity, currentList);
        return SpellResult.CAST;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        Mage mage = context.getMage();
        size = parameters.getInt("size", 1);
        useFire = parameters.getBoolean("fire", false);
        breakBlocks = parameters.getBoolean("break_blocks", true);

        size = (int)(mage.getRadiusMultiplier() * size);
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresBreakPermission()
    {
        return true;
    }
}
