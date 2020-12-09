package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.block.UndoQueue;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class UndoSpell extends TargetingSpell
{
    private String undoListName;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        int timeout = parameters.getInt("target_timeout", 0);
        boolean targetSelf = parameters.getBoolean("target_up_self", false);
        boolean targetDown = parameters.getBoolean("target_down_block", false);
        Entity targetEntity = target.getEntity();
        SpellResult result = SpellResult.CAST;
        if (targetSelf && isLookingUp()) {
            targetEntity = mage.getEntity();
            getCurrentCast().setTargetName(mage.getName());
            result = SpellResult.ALTERNATE_UP;
        }
        if (targetEntity != null && controller.isMage(targetEntity))
        {
            Mage targetMage = controller.getMage(targetEntity);

            Batch batch = targetMage.cancelPending();
            if (batch != null) {
                undoListName = (batch instanceof SpellBatch) ? ((SpellBatch)batch).getSpell().getName() : null;
                return SpellResult.ALTERNATE;
            }

            UndoQueue queue = targetMage.getUndoQueue();
            UndoList undoList = queue.undoRecent(timeout);
            if (undoList != null) {
                undoListName = undoList.getName();
            }
            return undoList != null ? result : SpellResult.NO_TARGET;
        }

        if (!parameters.getBoolean("target_blocks", true)) {
            return SpellResult.NO_TARGET;
        }

        Block targetBlock = target.getBlock();
        if (targetDown && isLookingDown()) {
            targetBlock = getLocation().getBlock();
        }
        if (targetBlock != null)
        {
            boolean targetAll = mage.isSuperPowered();
            if (targetAll)
            {
                UndoList undid = controller.undoRecent(targetBlock, timeout);
                if (undid != null)
                {
                    Mage targetMage = undid.getOwner();
                    undoListName = undid.getName();
                    if (targetMage != null) {
                        getCurrentCast().setTargetName(targetMage.getName());
                    }
                    return result;
                }
            }
            else
            {
                getCurrentCast().setTargetName(mage.getName());
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
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        return message.replace("$spell", undoListName == null ? "Unknown" : undoListName);
    }
}
