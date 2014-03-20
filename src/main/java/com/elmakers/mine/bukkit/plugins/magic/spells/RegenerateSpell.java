package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.RegenerateBatch;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.TargetType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class RegenerateSpell extends BlockSpell 
{
	private static final int DEFAULT_MAX_DIMENSION = 128;
	
	private Block targetBlock = null;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
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

		if (singleBlock)
		{
			deactivate();
			World world = targetBlock.getWorld();
			Chunk chunk = targetBlock.getChunk();
			world.regenerateChunk(chunk.getX(), chunk.getZ());
			
			return SpellResult.CAST;
		}

		if (this.targetBlock != null)
		{
			RegenerateBatch batch = new RegenerateBatch(this, this.targetBlock.getLocation(), targetBlock.getLocation());

			int maxDimension = parameters.getInteger("max_dimension", DEFAULT_MAX_DIMENSION);		
			maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);
			
			if (!batch.checkDimension(maxDimension))
			{
				return SpellResult.FAIL;
			}

			mage.addPendingBlockBatch(batch);
			
			deactivate();
			return SpellResult.CAST;
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
