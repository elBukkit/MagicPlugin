package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.configuration.ConfigurationSection;

public class SimpleBlockAction extends BaseSpellAction implements BlockAction
{
	protected final UndoList modified;
	
	public SimpleBlockAction(Spell spell, UndoList undoList)
	{
		modified = undoList;
		initialize(spell, null);
	}

	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		MageController controller = getController();
		if (controller != null)
		{
			controller.updateBlock(block);
		}
		if (modified != null)
		{
			modified.add(block);
		}
		return SpellResult.CAST;
	}

	public UndoList getBlocks()
	{
		return modified;
	}
}
