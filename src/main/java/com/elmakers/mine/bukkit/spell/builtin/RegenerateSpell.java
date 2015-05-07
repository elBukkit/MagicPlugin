package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.batch.RegenerateBatch;
import com.elmakers.mine.bukkit.spell.BlockSpell;

public class RegenerateSpell extends BlockSpell 
{
	private static final int DEFAULT_MAX_DIMENSION = 128;
	
	private Block targetBlock = null;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block targetBlock = getTargetBlock();

		if (targetBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock) || !hasBreakPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		if (targetLocation2 != null) {
			this.targetBlock = targetLocation2.getBlock();
		}

		if (this.targetBlock != null || getTargetType() == TargetType.BLOCK)
		{
			Block secondBlock = getTargetType() == TargetType.BLOCK ? targetBlock : this.targetBlock;
			RegenerateBatch batch = new RegenerateBatch(this, secondBlock.getLocation(), targetBlock.getLocation());

			int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);		
			maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);
			
			if (!batch.checkDimension(maxDimension))
			{
				return SpellResult.FAIL;
			}
			
			batch.setExpand(parameters.getBoolean("expand", false));

			boolean success = mage.addBatch(batch);
			
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
