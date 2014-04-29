package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BoundingBox;
import com.elmakers.mine.bukkit.spell.BlockSpell;

public class PortalSpell extends BlockSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Material blockType = target.getType();
		Block portalBase = target.getRelative(BlockFace.UP);
		blockType = portalBase.getType();
		if (blockType != Material.AIR)
		{
			portalBase = getPreviousBlock();
		}

		blockType = portalBase.getType();
		if (blockType != Material.AIR && blockType != Material.SNOW)
		{
			return SpellResult.NO_TARGET;		
		}

		controller.disablePhysics(1000);
		buildPortalBlocks(portalBase.getLocation(), BlockFace.NORTH);
		registerForUndo();

		return SpellResult.CAST;
	}

	protected void buildPortalBlocks(Location centerBlock, BlockFace facing)
	{
		Set<Material> destructible = mage.getController().getDestructibleMaterials();
		BoundingBox container = new BoundingBox(centerBlock.getBlockX(), centerBlock.getBlockY(), centerBlock.getBlockZ(), centerBlock.getBlockX() + 2, centerBlock.getBlockY() + 3, centerBlock.getBlockZ() + 1);
		container.fill(centerBlock.getWorld(), Material.PORTAL, destructible, getUndoList());
	}
}
