package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class DropAction extends BaseSpellAction {
    private static Material defaultTool = Material.DIAMOND_PICKAXE;
    private int dropCount;
    private boolean falling = true;
    private Collection<ItemStack> drops;
    private ItemStack toolItem;

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        Location target = context.getTargetLocation();
        if (target == null || drops == null) return;

        for (ItemStack drop : drops) {
            target.getWorld().dropItemNaturally(target, drop);
        }
        drops = null;
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        dropCount = parameters.getInt("drop_count", -1);
        falling = parameters.getBoolean("falling", true);
        String toolMaterialName = parameters.getString("tool", defaultTool.name());
        toolItem = null;
        try {
            ItemData toolData = context.getController().getOrCreateItem(toolMaterialName);
            if (toolData == null) {
                if (!toolMaterialName.isEmpty()) {
                    context.getLogger().warning("Invalid tool in drop action: " + toolMaterialName);
                }
            } else {
                toolItem = toolData.getItemStack();
            }
        } catch (Throwable ex) {
            context.getLogger().warning("Invalid tool in drop action: " + toolMaterialName);
        }
        if (toolItem == null) {
            toolItem = new ItemStack(defaultTool);
        }
        drops = new ArrayList<>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();

        if (!context.hasBreakPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }
        // Don't allow dropping temporary blocks
        UndoList blockUndoList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(block.getLocation());
        if (blockUndoList != null && blockUndoList.isScheduled()) {
            return SpellResult.NO_TARGET;
        }
        if (dropCount < 0 || drops.size() < dropCount) {
            drops.addAll(block.getDrops(toolItem));
        } else if (falling) {
            Location blockLocation = block.getLocation();
            Location blockCenter = new Location(blockLocation.getWorld(), blockLocation.getX() + 0.5, blockLocation.getY() + 0.5, blockLocation.getZ() + 0.5);
            FallingBlock falling = block.getWorld().spawnFallingBlock(blockCenter, block.getType(), block.getData());
            falling.setDropItem(false);
        }

        UndoList undoList = context.getUndoList();
        if (!undoList.isBypass()) {
            context.registerForUndo(block);
            context.clearAttachables(block);
        }
        BlockState prior = block.getState();
        block.setType(Material.AIR);
        if (undoList != null && !undoList.isScheduled()) {
            context.getController().logBlockChange(context.getMage(), prior, block.getState());
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresBreakPermission() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
