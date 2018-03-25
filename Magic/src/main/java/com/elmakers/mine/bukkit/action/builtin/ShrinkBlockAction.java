package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ShrinkBlockAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Block targetBlock = context.getTargetBlock();
        if (targetBlock == null) {
            return SpellResult.NO_TARGET;
        }
        MageController controller = context.getController();
        String blockSkin = controller.getBlockSkin(targetBlock.getType());
        if (blockSkin == null) return SpellResult.NO_TARGET;

        if (!context.hasBreakPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(targetBlock);

        dropHead(targetBlock.getLocation(), blockSkin, targetBlock.getType().name(), (byte)3);
        targetBlock.setType(Material.AIR);
        return SpellResult.CAST;
    }

    @SuppressWarnings("deprecation")
    protected void dropHead(Location location, String ownerName, String itemName, byte data) {
        ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, data);
        ItemMeta meta = shrunkenHead.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        if (meta instanceof SkullMeta && ownerName != null) {
            SkullMeta skullData = (SkullMeta)meta;
            skullData.setOwner(ownerName);
        }
        shrunkenHead.setItemMeta(meta);
        location.getWorld().dropItemNaturally(location, shrunkenHead);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
