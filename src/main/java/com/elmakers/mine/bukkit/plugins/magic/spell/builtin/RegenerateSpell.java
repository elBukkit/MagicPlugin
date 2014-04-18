package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.RegenerateBatch;
import com.elmakers.mine.bukkit.plugins.magic.spell.BlockSpell;

public class RegenerateSpell extends BlockSpell 
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

		if (singleBlock)
		{
			deactivate();
			World world = targetBlock.getWorld();
			Chunk chunk = targetBlock.getChunk();
			world.regenerateChunk(chunk.getX(), chunk.getZ());
			
			return SpellResult.CAST;
		}
		
		if (targetLocation2 != null) {
			this.targetBlock = targetLocation2.getBlock();
		}

		if (this.targetBlock != null)
		{
			RegenerateBatch batch = new RegenerateBatch(this, this.targetBlock.getLocation(), targetBlock.getLocation());

			int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);		
			maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);
			
			if (!batch.checkDimension(maxDimension))
			{
				return SpellResult.FAIL;
			}

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
