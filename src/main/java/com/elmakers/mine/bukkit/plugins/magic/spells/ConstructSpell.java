package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.blocks.ConstructBatch;
import com.elmakers.mine.bukkit.plugins.magic.blocks.ConstructionType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ConstructSpell extends Spell
{
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private int             timeToLive              = 0;
	private Set<Material>	indestructible		    = null;

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
		ConstructBatch batch = new ConstructBatch(this, target.getLocation(), type, radius, fill, material, data, indestructible);
		if (timeToLive > 0) {
			batch.setTimeToLive(timeToLive);
		}
		spells.addPendingBlockBatch(batch);
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
