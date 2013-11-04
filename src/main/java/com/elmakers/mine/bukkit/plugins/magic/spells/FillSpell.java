package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.BlockRecurse;
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
			this.targetBlock = null;

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
			castMessage("Filled " + action.getBlocks().size() + " blocks with " + material.name().toLowerCase());	
			return SpellResult.SUCCESS;
		}
		else if (singleBlock)
		{
			this.targetBlock = null;

			BlockList filledBlocks = new BlockList();

			filledBlocks.add(targetBlock);
			targetBlock.setType(material);
			targetBlock.setData(data);

			castMessage("Painting with " + material.name().toLowerCase());
			spells.addToUndoQueue(player, filledBlocks);
			return SpellResult.SUCCESS;
		}

		if (this.targetBlock != null)
		{
			int deltax = targetBlock.getX() - this.targetBlock.getX();
			int deltay = targetBlock.getY() - this.targetBlock.getY();
			int deltaz = targetBlock.getZ() - this.targetBlock.getZ();

			int absx = Math.abs(deltax);
			int absy = Math.abs(deltay);
			int absz = Math.abs(deltaz);

			int maxDimension = player.isOp() ? defaultMaxDimension * 10 : defaultMaxDimension;
			int maxVolume = player.isOp() ? defaultMaxVolume * 10 : defaultMaxVolume;

			if (maxDimension > 0 && (absx > maxDimension || absy > maxDimension || absz > maxDimension))
			{
				castMessage("Dimension is too big!");
				return SpellResult.FAILURE;
			}

			if (maxVolume > 0 && absx * absy * absz > maxVolume)
			{
				castMessage("Volume is too big!");
				return SpellResult.FAILURE;
			}

			int dx = (int)Math.signum(deltax);
			int dy = (int)Math.signum(deltay);
			int dz = (int)Math.signum(deltaz);

			absx++;
			absy++;
			absz++;

			if (!overrideMaterial)
			{
				material = this.targetBlock.getType();
				data = this.targetBlock.getData();
			}

			BlockList filledBlocks = new BlockList();
			castMessage("Filling " + absx + "x" + absy + "x" + absz + " area with " + material.name().toLowerCase());
			int x = this.targetBlock.getX();
			int y = this.targetBlock.getY();
			int z = this.targetBlock.getZ();
			for (int ix = 0; ix < absx; ix++)
			{
				for (int iy = 0; iy < absy; iy++)
				{
					for (int iz = 0; iz < absz; iz++)
					{
						Block block = getBlockAt(x + ix * dx, y + iy * dy, z + iz * dz);
						if (!hasBuildPermission(block)) continue;
						
						filledBlocks.add(block);
						block.setType(material);
						block.setData(data);
					}
				}
			}
			spells.addToUndoQueue(player, filledBlocks);

			this.targetBlock = null;
			return SpellResult.SUCCESS;
		}
		else
		{
			this.targetBlock = targetBlock;
			if (!overrideMaterial)
			{
				material = targetBlock.getType();
			}
			castMessage("Cast again to fill with " + material.name().toLowerCase());
			return SpellResult.SUCCESS;
		}
	}

	@Override
	public void onCancel()
	{
		if (targetBlock != null)
		{
			castMessage("Cancelled fill");
			targetBlock = null;
		}
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		defaultMaxDimension = properties.getInteger("max_dimension", defaultMaxDimension);
		defaultMaxVolume = properties.getInteger("max_volume", defaultMaxVolume);
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
}
