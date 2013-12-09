package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ConstructSpell extends Spell
{
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private int             timeToLive              = 0;
	private Set<Material>	indestructible		    = null;

	public enum ConstructionType
	{
		SPHERE,
		CUBOID,
		PYRAMID,
		UNKNOWN;

		public static ConstructionType parseString(String s, ConstructionType defaultType)
		{
			ConstructionType construct = defaultType;
			for (ConstructionType t : ConstructionType.values())
			{
				if (t.name().equalsIgnoreCase(s))
				{
					construct = t;
				}
			}
			return construct;
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		targetThrough(Material.GLASS);
		Block target = getTarget().getBlock();

		if (target == null)
		{
			initializeTargeting(player);
			noTargetThrough(Material.GLASS);
			target = getTarget().getBlock();
		}

		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (parameters.containsKey("y_offset")) {
			target = target.getRelative(BlockFace.UP, parameters.getInt("y_offset", 0));
		}
		
		if (!hasBuildPermission(target)) {
			castMessage("You don't have permission to build here.");
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Material material = target.getType();
		byte data = target.getData();

		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
		}

		ConstructionType conType = defaultConstructionType;

		boolean hollow = false;
		String fillType = (String)parameters.getString("fill", "");
		hollow = fillType.equals("hollow");

		Material materialOverride = parameters.getMaterial("material");
		if (materialOverride != null)
		{
			material = materialOverride;
			data = 0;
		}

		int radius = parameters.getInt("radius", defaultRadius);
		radius = parameters.getInt("size", radius);
		String typeString = parameters.getString("type", "");
		
		// radius = (int)(playerSpells.getPowerMultiplier() * radius);

		ConstructionType testType = ConstructionType.parseString(typeString, ConstructionType.UNKNOWN);
		if (testType != ConstructionType.UNKNOWN)
		{
			conType = testType;
		}

		fillArea(target, radius, material, data, !hollow, conType);

		return SpellResult.SUCCESS;
	}

	public void fillArea(Block target, int radius, Material material, byte data, boolean fill, ConstructionType type)
	{
		BlockList constructedBlocks = new BlockList();

		for (int y = 0; y <= radius; ++y)
		{
			for (int x = 0; x <= radius; ++x)
			{
				for (int z = 0; z <= radius; ++z)
				{
					boolean fillBlock = false;
					switch(type) {
						case SPHERE:
							int maxDistanceSquared = radius * radius;
							float mx = (float)x - 0.5f;
							float my = (float)y - 0.5f;
							float mz = (float)z - 0.5f;
							
							int distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
							if (fill)
							{
								fillBlock = distanceSquared <= maxDistanceSquared;
							} 
							else 
							{
								mx++;
								my++;
								mz++;
								int outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
								fillBlock = maxDistanceSquared >= distanceSquared && maxDistanceSquared <= outerDistanceSquared;
							}
							//spells.getLog().info("(" + x + "," + y + "," + z + ") : " + fillBlock + " = " + distanceSquared + " : " + maxDistanceSquared);
							break;
						case PYRAMID:
							int elevation = radius - y;
							if (fill) {
								fillBlock = (x <= elevation) && (z <= elevation);
							} else {
								fillBlock = (x == elevation && z <= elevation) || (z == elevation && x <= elevation);
							}
							break;
						default: 
							fillBlock = fill ? true : (x == radius || y == radius || z == radius);
							break;
					}
					if (fillBlock)
					{
						constructBlock(x, y, z, target, material, data, constructedBlocks);
						constructBlock(-x, y, z, target, material, data, constructedBlocks);
						constructBlock(x, -y, z, target, material, data, constructedBlocks);
						constructBlock(x, y, -z, target,  material, data, constructedBlocks);
						constructBlock(-x, -y, z, target, material, data, constructedBlocks);
						constructBlock(x, -y, -z, target, material, data, constructedBlocks);
						constructBlock(-x, y, -z, target, material, data, constructedBlocks);
						constructBlock(-x, -y, -z, target, material, data, constructedBlocks);
					}
				}
			}
		}

		if (timeToLive == 0)
		{
			spells.addToUndoQueue(player, constructedBlocks);
		}
		else
		{
			constructedBlocks.setTimeToLive(timeToLive);
			spells.scheduleCleanup(constructedBlocks);
		}
		castMessage("Constructed " + constructedBlocks.size() + "blocks");
	}

	public int getDistanceSquared(int x, int y, int z)
	{
		return x * x + y * y + z * z;
	}

	@SuppressWarnings("deprecation")
	public void constructBlock(int dx, int dy, int dz, Block centerPoint, Material material, byte data, BlockList constructedBlocks)
	{
		int x = centerPoint.getX() + dx;
		int y = centerPoint.getY() + dy;
		int z = centerPoint.getZ() + dz;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isDestructible(block))
		{
			return;
		}
		if (!hasBuildPermission(block)) 
		{
			return;
		}
		constructedBlocks.add(block);
		block.setType(material);
		block.setData(data);
	}

	public boolean isDestructible(Block block)
	{
		return spells.getDestructibleMaterials().contains(block.getType()) && !indestructible.contains(block.getType());
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		timeToLive = properties.getInt("undo", timeToLive);
		indestructible = properties.getMaterials("indestructible", "");
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
}
