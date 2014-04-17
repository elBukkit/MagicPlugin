package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.BoundingBox;
import com.elmakers.mine.bukkit.plugins.magic.spell.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;

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

		int timeToLive = parameters.getInt("undo", 5000);
		BlockList portalBlocks = new BlockList();
		portalBlocks.setTimeToLive(timeToLive);
		controller.disablePhysics(1000);
		buildPortalBlocks(portalBase.getLocation(), BlockFace.NORTH, portalBlocks);
		registerForUndo(portalBlocks);

		return SpellResult.CAST;
	}

	protected void buildPortalBlocks(Location centerBlock, BlockFace facing, BlockList blockList)
	{
		Set<Material> destructible =mage.getController().getDestructibleMaterials();
		BoundingBox container = new BoundingBox(centerBlock.getBlockX(), centerBlock.getBlockY(), centerBlock.getBlockZ(), centerBlock.getBlockX() + 2, centerBlock.getBlockY() + 3, centerBlock.getBlockZ() + 1);
		container.fill(centerBlock.getWorld(), Material.PORTAL, destructible, blockList);
	}
}
