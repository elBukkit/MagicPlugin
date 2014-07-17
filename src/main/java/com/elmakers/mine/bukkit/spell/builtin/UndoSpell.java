package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.block.UndoQueue;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.batch.SpellBatch;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class UndoSpell extends TargetingSpell
{
	private String undoListName;
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
        int timeout = parameters.getInt("target_timeout", 0);
		if (target.hasEntity() && controller.isMage(target.getEntity()))
		{
			Mage targetMage = controller.getMage(target.getEntity());

            BlockBatch batch = targetMage.cancelPending();
            if (batch != null) {
                undoListName = (batch instanceof SpellBatch) ? ((SpellBatch)batch).getSpell().getName() : null;
                return SpellResult.CAST;
            }

            UndoQueue queue = targetMage.getUndoQueue();
			UndoList undoList = queue.undoRecent(timeout);
			if (undoList != null) {
				undoListName = undoList.getName();
			}
			return undoList != null ? SpellResult.CAST : SpellResult.NO_TARGET;
		}

        if (!parameters.getBoolean("target_blocks", true)) {
            return SpellResult.NO_TARGET;
        }
		
		Block targetBlock = isLookingDown() ? getLocation().getBlock() : target.getBlock();
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
					setTargetName(targetMage.getName());
					return SpellResult.CAST;
				}
			}
			else
			{
				setTargetName(mage.getName());
				UndoList undoList = mage.undo(targetBlock);
                if (undoList != null) {
                    undoListName = undoList.getName();
                    return SpellResult.CAST;
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
