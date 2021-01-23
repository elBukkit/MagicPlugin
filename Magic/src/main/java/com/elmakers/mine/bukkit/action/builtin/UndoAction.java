package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.block.UndoQueue;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class UndoAction extends BaseSpellAction
{
    private String undoListName;
    private int timeout;
    private int blockTimeout;
    private String targetSpellKey;
    private boolean targetSelf;
    private boolean targetDown;
    private boolean targetBlocks;
    private boolean targetOtherBlocks;
    private boolean cancel;
    private boolean sort;
    private String adminPermission;
    private int undoOldest;
    private int undoToSize;

    // State
    private int undone;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        timeout = parameters.getInt("target_timeout", 0);
        blockTimeout = parameters.getInt("target_block_timeout", timeout);

        // TODO: Use a ContextAction instead?
        targetSelf = parameters.getBoolean("target_caster", false);
        targetDown = parameters.getBoolean("target_down", false);
        targetBlocks = parameters.getBoolean("target_blocks", true);
        targetOtherBlocks = parameters.getBoolean("target_other_blocks", false);
        cancel = parameters.getBoolean("cancel", true);
        targetSpellKey = parameters.getString("target_spell", null);
        adminPermission = parameters.getString("admin_permission", null);

        undoOldest = parameters.getInt("undo_oldest", 0);
        undoToSize = parameters.getInt("undo_to", 0);
        sort = parameters.getBoolean("sort", false);
    }
    @Override
    public void reset(CastContext context) {
        super.reset(context);
        undone = 0;
    }

    protected SpellResult performNew(CastContext context)
    {
        UndoList undoList = context.getUndoList();
        int totalSize = undoList.size() + undoList.getRunnableCount();
        if (totalSize == 0)
        {
            return SpellResult.NO_ACTION;
        }

        boolean undid = false;
        while (context.getWorkAllowed() > 0) {
            if (undoToSize > 0 && totalSize <= undoToSize) break;
            if (undoList.size() == 0) {
                if (undoList.undoNextRunnable() == null) break;
            } else {
                if (undoList.undoNext(false) == null) break;
            }
            undid = true;
            totalSize--;
            undone++;

            context.addWork(10);
            if (undoOldest > 0 && undone > undoOldest) {
                break;
            }
        }
        if (!undid) return SpellResult.NO_ACTION;

        return undoOldest == 0 || undone >= undoOldest ? SpellResult.CAST : SpellResult.PENDING;
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (sort)
        {
            UndoList undoList = context.getUndoList();
            if (undoList instanceof com.elmakers.mine.bukkit.block.UndoList) {
                MaterialSetManager materialSets = context.getController().getMaterialSetManager();
                ((com.elmakers.mine.bukkit.block.UndoList)undoList).sort(materialSets.getMaterialSet("all_attachable"));
            }
            return SpellResult.CAST;
        }
        // Start of new functionality
        if (undoOldest > 0 || undoToSize > 0)
        {
            return performNew(context);
        }

        // Old functionality- this should be converted into an action that processes
        // blocks instead of creating a separate batch.
        Entity targetEntity = context.getTargetEntity();

        SpellResult result = SpellResult.CAST;
        Mage mage = context.getMage();
        if (targetSelf) {
            targetEntity = context.getEntity();
            context.setTargetName(mage.getName());
            result = SpellResult.ALTERNATE_UP;
        }
        MageController controller = context.getController();
        if (targetEntity != null && controller.isMage(targetEntity))
        {
            Mage targetMage = controller.getMage(targetEntity);
            mage.sendDebugMessage(ChatColor.AQUA + "Undo checking last spell of "
                    + ChatColor.GOLD + targetMage.getName() + ChatColor.AQUA + " with timeout of "
                    + ChatColor.YELLOW + timeout + ChatColor.AQUA + " for target spellKey"
                    + ChatColor.BLUE + targetSpellKey, 2);

            Batch batch = targetMage.cancelPending(targetSpellKey, false);
            if (batch != null) {
                undoListName = batch.getName();
                if (cancel)
                {
                    return SpellResult.DEACTIVATE;
                }
            }

            UndoQueue queue = targetMage.getUndoQueue();
            UndoList undoList = queue.undoRecent(timeout, targetSpellKey);
            if (undoList != null) {
                undoListName = undoList.getName();
            }
            return undoList != null ? result : SpellResult.NO_TARGET;
        }

        if (!targetBlocks) {
            return SpellResult.NO_TARGET;
        }

        Block targetBlock = context.getTargetBlock();
        if (targetDown) {
            targetBlock = context.getLocation().getBlock();
        }
        if (targetBlock != null)
        {
            boolean undoAny = targetOtherBlocks;
            undoAny = undoAny || (adminPermission != null && context.getController().hasPermission(context.getMage().getCommandSender(), adminPermission));
            undoAny = undoAny || mage.isSuperPowered();
            if (undoAny)
            {
                mage.sendDebugMessage(ChatColor.AQUA + "Looking for recent cast at "
                        + ChatColor.GOLD + targetBlock + ChatColor.AQUA + " with timeout of "
                        + ChatColor.YELLOW + blockTimeout, 2);

                UndoList undid = controller.undoRecent(targetBlock, blockTimeout);
                if (undid != null)
                {
                    Mage targetMage = undid.getOwner();
                    undoListName = undid.getName();
                    if (targetMage != null) {
                        context.setTargetName(targetMage.getName());
                    }
                    return result;
                }
            }
            else
            {
                mage.sendDebugMessage(ChatColor.AQUA + "Looking for recent self-cast at "
                        + ChatColor.GOLD + targetBlock, 2);

                context.setTargetName(mage.getName());
                UndoList undoList = mage.undo(targetBlock);
                if (undoList != null) {
                    undoListName = undoList.getName();
                    return result;
                }
            }
        }

        return SpellResult.NO_TARGET;
    }

    @Override
    public String transformMessage(String message) {
        return message.replace("$spell", undoListName == null ? "Unknown" : undoListName);
    }
}
