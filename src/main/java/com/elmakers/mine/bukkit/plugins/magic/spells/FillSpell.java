package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.BlockRecurse;
import com.elmakers.mine.bukkit.blocks.FillBatch;
import com.elmakers.mine.bukkit.blocks.ReplaceMaterialAction;
import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FillSpell extends BrushSpell 
{
	private static final int DEFAULT_MAX_DIMENSION = 128;
	private static final int DEFAULT_MAX_VOLUME = 512;	
	
	private Block targetBlock = null;
	private final BlockRecurse blockRecurse = new BlockRecurse();

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		noTargetThrough(Material.STATIONARY_WATER);
		noTargetThrough(Material.WATER);
		Block targetBlock = getTargetBlock();
		boolean singleBlock = false;
		boolean recurse = false;

		String typeString = parameters.getString("type", "");
		singleBlock = typeString.equals("single");
		recurse = typeString.equals("recurse");

		if (targetBlock == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		MaterialBrush buildWith = getMaterialBrush();

		Material material = buildWith.getMaterial();
		byte data = buildWith.getData();

		if (recurse)
		{
			deactivate();
			int size = parameters.getInt("size", 8);
			size = (int)(mage.getRadiusMultiplier() * size);
			blockRecurse.setMaxRecursion(size);

			Material targetMaterial = targetBlock.getType();
			ReplaceMaterialAction action = new ReplaceMaterialAction(mage, targetBlock, buildWith);

			// A bit hacky, but is very handy!
			if (targetMaterial == Material.STATIONARY_WATER)
			{
				action.addReplaceable(Material.WATER);
			}
			else if (targetMaterial == Material.WATER)
			{
				action.addReplaceable(Material.STATIONARY_WATER);
			}
			else if (targetMaterial == Material.STATIONARY_LAVA)
			{
				action.addReplaceable(Material.LAVA);
			}
			else if (targetMaterial == Material.LAVA)
			{
				action.addReplaceable(Material.STATIONARY_LAVA);
			}
			blockRecurse.recurse(targetBlock, action);
			controller.addToUndoQueue(getPlayer(), action.getBlocks());
			controller.updateBlock(targetBlock);
			castMessage("Filled " + action.getBlocks().size() + " blocks with " + material.name().toLowerCase());	
			return SpellResult.SUCCESS;
		}
		else if (singleBlock)
		{
			deactivate();

			BlockList filledBlocks = new BlockList();

			filledBlocks.add(targetBlock);
			targetBlock.setType(material);
			targetBlock.setData(data);
			
			controller.updateBlock(targetBlock);

			castMessage("Painting with " + material.name().toLowerCase());
			controller.addToUndoQueue(getPlayer(), filledBlocks);
			return SpellResult.SUCCESS;
		}

		if (this.targetBlock != null)
		{
			FillBatch batch = new FillBatch(this, targetBlock.getLocation(), this.targetBlock.getLocation(), buildWith);

			int maxDimension = parameters.getInteger("max_dimension", DEFAULT_MAX_DIMENSION);
			int maxVolume = parameters.getInteger("max_volume", DEFAULT_MAX_VOLUME);
			
			maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);
			maxVolume = (int)(mage.getConstructionMultiplier() * maxVolume);
			
			if (!batch.checkDimension(maxDimension))
			{
				sendMessage("Dimension is too big!");
				return SpellResult.FAILURE;
			}

			if (!batch.checkVolume(maxVolume))
			{
				sendMessage("Volume is too big!");
				return SpellResult.FAILURE;
			}

			controller.addPendingBlockBatch(batch);
			
			deactivate();
			return SpellResult.SUCCESS;
		}
		else
		{
			Location effectLocation = targetBlock.getLocation();
			effectLocation.add(0.5f, 0.5f, 0.5f);
			EffectUtils.playEffect(effectLocation, ParticleType.HAPPY_VILLAGER, 0.3f, 0.3f, 0.3f, 1, 16);
			this.targetBlock = targetBlock;
			activate();
			buildWith.setTarget(targetBlock.getLocation());
			castMessage("Cast again to fill with " + material.name().toLowerCase());
			return SpellResult.SUCCESS;
		}
	}

	@Override
	public boolean onCancel()
	{
		if (targetBlock != null)
		{
			sendMessage("Cancelled fill");
			targetBlock = null;
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onDeactivate() {
		targetBlock = null;
	}
}
