package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class ConstructSpell extends Spell
{
	static final String		DEFAULT_DESTRUCTIBLES	= "1,2,3,8,9,10,11,12,13,87,88";

	private List<Material>	destructibleMaterials	= new ArrayList<Material>();
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
    
	public ConstructSpell()
	{
		addVariant("shell", Material.BOWL, getCategory(), "Create a large spherical shell", "sphere hollow 10");
		addVariant("box", Material.WOODEN_DOOR, getCategory(), "Create a large box", "cuboid hollow 6");
		addVariant("superblob", Material.CLAY_BRICK, getCategory(), "Create a large solid sphere", "sphere 8");
		addVariant("sandblast", Material.SANDSTONE, getCategory(), "Drop a big block of sand", "cuboid 4 with sand");
	}
	
	public enum ConstructionType
	{
		SPHERE,
		CUBOID,
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
	
	@Override
	public boolean onCast(String[] parameters)
	{
		setMaxRange(defaultSearchDistance, true);
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
            castMessage(player, "No target");
            return false;
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
		int radius = defaultRadius;
		boolean hollow = false;
		
		for (int i = 0; i < parameters.length; i++)
		{
			String parameter = parameters[i];

			if (parameter.equalsIgnoreCase("hollow"))
			{
				hollow = true;
				continue;
			}
			
			if (parameter.equalsIgnoreCase("with") && i < parameters.length - 1)
			{
				String materialName = parameters[i + 1];
				data = 0;
				material = getMaterial(materialName, spells.getBuildingMaterials());
				i++;
				continue;
			}
			
			// try radius
			try
			{
				radius = Integer.parseInt(parameter);
				if (radius > maxRadius && maxRadius > 0)
				{
					radius = maxRadius;
				}
				
				// Assume number, ok to continue
				continue;
			} 
			catch(NumberFormatException ex)
			{
			}

			// Try con type
			{
				ConstructionType testType = ConstructionType.parseString(parameter, ConstructionType.UNKNOWN);
				if (testType != ConstructionType.UNKNOWN)
				{
					conType = testType;
				}
			}
		}
		
		switch (conType)
		{
			case SPHERE: constructSphere(target, radius, material, data, !hollow); break;
			case CUBOID: constructCuboid(target, radius, material, data, !hollow); break;
			default : return false;
		}
		
		return true;
	}
	
	public void constructCuboid(Block target, int radius, Material material, byte data, boolean fill)
	{
		fillArea(target, radius, material, data, fill, false);
	}
	
	public void constructSphere(Block target, int radius, Material material, byte data, boolean fill)
	{
		fillArea(target, radius, material, data, fill, true);
	}
	
	public void fillArea(Block target, int radius, Material material, byte data, boolean fill, boolean sphere)
	{
		BlockList constructedBlocks = new BlockList();
		int diameter = radius * 2;
		int midX = (diameter - 1) / 2;
		int midY = (diameter - 1) / 2;
		int midZ = (diameter - 1) / 2;
		int diameterOffset = diameter - 1;
		int radiusSquared = (radius - 1) * (radius - 1);

		for (int x = 0; x < radius; ++x)
		{
			for (int y = 0; y < radius; ++y)
			{
				for (int z = 0; z < radius; ++z)
				{
					boolean fillBlock = false;
					
					if (sphere)
					{
						int distanceSquared = getDistanceSquared(x - midX, y - midY, z - midZ);
						fillBlock = distanceSquared <= radiusSquared;
						if (!fill)
						{
							fillBlock = fillBlock && distanceSquared >= radiusSquared - 1;
						}
					}
					else
					{
						fillBlock = fill ? true : (x == 0 || y == 0 || z == 0);
					}
					if (fillBlock)
					{
						constructBlock(x, y, z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, y, z, target, radius, material, data, constructedBlocks);
						constructBlock(x, diameterOffset - y, z, target, radius, material, data, constructedBlocks);
						constructBlock(x, y, diameterOffset - z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, diameterOffset - y, z, target, radius, material, data, constructedBlocks);
						constructBlock(x, diameterOffset - y, diameterOffset - z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, y, diameterOffset - z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, material, data, constructedBlocks);
					}
				}
			}
		}

		spells.addToUndoQueue(player, constructedBlocks);
		castMessage(player, "Constructed " + constructedBlocks.size() + "blocks");
	}
	
	public int getDistanceSquared(int x, int y, int z)
	{
		return x * x + y * y + z * z;
	}

	public void constructBlock(int dx, int dy, int dz, Block centerPoint, int radius, Material material, byte data, BlockList constructedBlocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isDestructible(block))
		{
			return;
		}
		constructedBlocks.add(block);
		block.setType(material);
		block.setData(data);
	}

	public boolean isDestructible(Block block)
	{
		if (block.getType() == Material.AIR)
			return true;

		return destructibleMaterials.contains(block.getType());
	}

	@Override
	public String getName()
	{
		return "blob";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Create a solid blob";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		destructibleMaterials = PluginProperties.parseMaterials(DEFAULT_DESTRUCTIBLES);
		defaultConstructionType = ConstructionType.parseString(properties.getString("spells-construct-default", ""), defaultConstructionType);
		defaultRadius = properties.getInteger("spells-construct-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-construct-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-constructs-search-distance", defaultSearchDistance);
	}

	@Override
	public Material getMaterial()
	{
		return Material.CLAY_BALL;
	}

}
