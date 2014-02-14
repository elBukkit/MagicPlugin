package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.FillBatch;
import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.TargetType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FillSpell extends BrushSpell 
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
			castMessage("No target");
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
			filledBlocks.setTimeToLive(parameters.getInt("undo", 0));
			filledBlocks.add(targetBlock);

			buildWith.setTarget(targetBlock.getLocation());
			buildWith.update(mage, targetBlock.getLocation());
			buildWith.modify(targetBlock);
			
			controller.updateBlock(targetBlock);

			Material material = buildWith.getMaterial();
			castMessage("Painting with " + material.name().toLowerCase());
			mage.registerForUndo(filledBlocks);
			
			return SpellResult.CAST;
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

			int maxDimension = parameters.getInteger("max_dimension", DEFAULT_MAX_DIMENSION);
			
			maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);
			
			if (!batch.checkDimension(maxDimension))
			{
				sendMessage("Dimension is too big!");
				return SpellResult.FAIL;
			}

			mage.addPendingBlockBatch(batch);
			
			deactivate();
			return SpellResult.CAST;
		}
		else
		{
			Location effectLocation = targetBlock.getLocation();
			effectLocation.add(0.5f, 0.5f, 0.5f);
			EffectUtils.playEffect(effectLocation, ParticleType.HAPPY_VILLAGER, 0.3f, 0.3f, 0.3f, 1, 16);
			this.targetBlock = targetBlock;
			activate();
			
			// Note we don't set the target until the second cast.
			Material material = buildWith.getMaterial();
			castMessage("Cast again to fill with " + material.name().toLowerCase());
			
			return SpellResult.TARGET_SELECTED;
		}
	}

	@Override
	public boolean onCancel()
	{
		if (targetBlock != null)
		{
			sendMessage("Cancelled fill");
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
