package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.blocks.FillBatch;
import com.elmakers.mine.bukkit.utilities.BlockRecurse;
import com.elmakers.mine.bukkit.utilities.EffectUtils;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.ReplaceMaterialAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FillSpell extends Spell 
{
	private int defaultMaxDimension = 128;
	private int defaultMaxVolume = 512;
	private Block targetBlock = null;
	private final BlockRecurse blockRecurse = new BlockRecurse();

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		noTargetThrough(Material.STATIONARY_WATER);
		noTargetThrough(Material.WATER);
		Block targetBlock = getTargetBlock();
		Material material = Material.AIR;
		byte data = 0;
		boolean singleBlock = false;
		boolean recurse = false;

		boolean overrideMaterial = false;

		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
			overrideMaterial = true;
		}
		String typeString = parameters.getString("type", "");
		singleBlock = typeString.equals("single");
		recurse = typeString.equals("recurse");

		Material materialOverride = parameters.getMaterial("material");
		if (materialOverride != null)
		{
			material = materialOverride;
			data = 0;
			overrideMaterial = true;
		}

		if (targetBlock == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) {
			castMessage("You don't have permission to build here.");
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		if (recurse)
		{
			deactivate();
			int size = parameters.getInt("size", 8);
			blockRecurse.setMaxRecursion(size);

			PlayerSpells playerSpells = spells.getPlayerSpells(player);
			Material targetMaterial = targetBlock.getType();
			ReplaceMaterialAction action = new ReplaceMaterialAction(playerSpells, targetBlock, material, data);

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
			spells.addToUndoQueue(player, action.getBlocks());
			spells.updateBlock(targetBlock);
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
			
			spells.updateBlock(targetBlock);

			castMessage("Painting with " + material.name().toLowerCase());
			spells.addToUndoQueue(player, filledBlocks);
			return SpellResult.SUCCESS;
		}

		if (this.targetBlock != null)
		{
			if (!overrideMaterial)
			{
				material = this.targetBlock.getType();
				data = this.targetBlock.getData();
			}

			FillBatch batch = new FillBatch(this, targetBlock.getLocation(), this.targetBlock.getLocation(), material, data);

			int maxDimension = player.isOp() ? defaultMaxDimension * 10 : defaultMaxDimension;
			int maxVolume = player.isOp() ? defaultMaxVolume * 10 : defaultMaxVolume;
			
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

			castMessage("Filling " + batch.getXSize() + "x" +  batch.getYSize() + "x" +  batch.getZSize() + " area with " + material.name().toLowerCase());
			
			spells.addPendingBlockBatch(batch);
			
			deactivate();
			return SpellResult.SUCCESS;
		}
		else
		{
			Location effectLocation = targetBlock.getLocation();
			effectLocation.add(0.5f, 0.5f, 0.5f);
			EffectUtils.playEffect(effectLocation, ParticleType.HAPPY_VILLAGER, 0.3f, 0.3f, 0.3f, 1, 16);
			this.targetBlock = targetBlock;
			if (!overrideMaterial)
			{
				material = targetBlock.getType();
			}
			activate();
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
	public void onLoadTemplate(ConfigurationNode properties)  
	{
		defaultMaxDimension = properties.getInteger("max_dimension", defaultMaxDimension);
		defaultMaxVolume = properties.getInteger("max_volume", defaultMaxVolume);
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
	
	@Override
	public void onDeactivate() {
		targetBlock = null;
	}
}
