package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.BlockRecurse;
import com.elmakers.mine.bukkit.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.ReplaceMaterialAction;

public class FillSpell extends Spell 
{
	private int defaultMaxDimension = 128;
	private int defaultMaxVolume = 512;
	private Block targetBlock = null;
	private final BlockRecurse blockRecurse = new BlockRecurse();
	
	@Override
	public boolean onCast(String[] parameters) 
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
		
		for (int i = 0; i < parameters.length; i++)
		{
			String parameter = parameters[i];
			if (parameter.equalsIgnoreCase("single"))
			{
				singleBlock = true;
				continue;
			}
			if (parameter.equalsIgnoreCase("recurse"))
			{
				recurse = true;
				continue;
			}
			if (parameter.equalsIgnoreCase("with") && i < parameters.length - 1)
			{
				String materialName = parameters[i + 1];
				data = 0;
				material = getMaterial(materialName, spells.getBuildingMaterials());
				overrideMaterial = true;
				i++;
				continue;
			}
		}
		
		if (targetBlock == null) 
		{
			castMessage(player, "No target");
			return false;
		}
	
		if (recurse)
		{
		    this.targetBlock = null;
		    
		    Material targetMaterial = targetBlock.getType();
			ReplaceMaterialAction action = new ReplaceMaterialAction(targetBlock, material, data);
			
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
			castMessage(player, "Filled " + action.getBlocks().size() + " blocks with " + material.name().toLowerCase());	
			return true;
		}
		else if (singleBlock)
		{
		    this.targetBlock = null;
        
			BlockList filledBlocks = new BlockList();
			
			filledBlocks.add(targetBlock);
			targetBlock.setType(material);
			targetBlock.setData(data);
			
			castMessage(player, "Painting with " + material.name().toLowerCase());
			spells.addToUndoQueue(player, filledBlocks);
			return true;
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
				player.sendMessage("Dimension is too big!");
				return false;
			}

			if (maxVolume > 0 && absx * absy * absz > maxVolume)
			{
				player.sendMessage("Volume is too big!");
				return false;
			}
			
			int dx = (int)Math.signum(deltax);
			int dy = (int)Math.signum(deltay);
			int dz = (int)Math.signum(deltaz);
			
			absx++;
			absy++;
			absz++;
			
			if (!overrideMaterial)
			{
			    material = targetBlock.getType();
			    data = targetBlock.getData();
			}
			
			BlockList filledBlocks = new BlockList();
			castMessage(player, "Filling " + absx + "x" + absy + "x" + absz + " area with " + material.name().toLowerCase());
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
						filledBlocks.add(block);
						block.setType(material);
						block.setData(data);
					}
				}
			}
			spells.addToUndoQueue(player, filledBlocks);
			
			this.targetBlock = null;
			return true;
		}
		else
		{
		    this.targetBlock = targetBlock;
			if (!overrideMaterial)
			{
				material = targetBlock.getType();
			}
			castMessage(player, "Cast again to fill with " + material.name().toLowerCase());
			return true;
		}
	}
	
	@Override
	public void onCancel()
	{
		if (targetBlock != null)
		{
			player.sendMessage("Cancelled fill");
			targetBlock = null;
		}
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultMaxDimension = properties.getInteger("spells-fill-max-dimension", defaultMaxDimension);
		defaultMaxVolume = properties.getInteger("spells-fill-max-volume", defaultMaxVolume);
	}
}
