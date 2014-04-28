package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.batch.FillBatch;
import com.elmakers.mine.bukkit.plugins.magic.spell.BrushSpell;

public class FillSpell extends BrushSpell 
{
	private static final int DEFAULT_MAX_DIMENSION = 128;
	
	private Block targetBlock = null;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block targetBlock = getTargetBlock();
		boolean singleBlock = getTargetType() != TargetType.SELECT;

		if (targetBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		MaterialBrush buildWith = getMaterialBrush();
		if (singleBlock)
		{
			deactivate();

			BlockList filledBlocks = new BlockList();
			int undoTime = parameters.getInt("undo", 0);
			undoTime = parameters.getInt("u", undoTime);
			filledBlocks.setTimeToLive(undoTime);
			filledBlocks.add(targetBlock);

			buildWith.setTarget(targetBlock.getLocation());
			buildWith.update(mage, targetBlock.getLocation());
			buildWith.modify(targetBlock);
			
			controller.updateBlock(targetBlock);
			registerForUndo(filledBlocks);
			
			return SpellResult.CAST;
		}
		
		if (targetLocation2 != null) {
			this.targetBlock = targetLocation2.getBlock();
		}

		if (this.targetBlock != null)
		{
			// Update the brush using the center of the fill volume. This is to make
			// Replicate, clone, map and schematic work consistently with the construction spells.
			Location centerLocation = targetBlock.getLocation();
			Location secondLocation = this.targetBlock.getLocation();
			centerLocation.setX(Math.floor((centerLocation.getX() + secondLocation.getX()) / 2));
			centerLocation.setY(Math.floor((centerLocation.getY() + secondLocation.getY()) / 2));
			centerLocation.setZ(Math.floor((centerLocation.getZ() + secondLocation.getZ()) / 2));
			buildWith.setTarget(this.targetBlock.getLocation(), centerLocation);
			
			FillBatch batch = new FillBatch(this, secondLocation, targetBlock.getLocation(), buildWith);

			int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);	
			maxDimension = parameters.getInt("md", maxDimension);	
			maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);
			
			if (!batch.checkDimension(maxDimension))
			{
				return SpellResult.FAIL;
			}
			batch.setTimeToLive(parameters.getInt("undo", 0));
			boolean success = mage.addPendingBlockBatch(batch);
			
			deactivate();
			return success ? SpellResult.CAST : SpellResult.FAIL;
		}
		else
		{
			this.targetBlock = targetBlock;
			activate();
			return SpellResult.TARGET_SELECTED;
		}
	}

	@Override
	public boolean onCancel()
	{
		if (targetBlock != null)
		{
			deactivate();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onDeactivate() {
		targetBlock = null;
	}
}
