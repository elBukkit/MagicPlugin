package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BridgeSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 16;

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to bridge
			castMessage("You need to be standing on something");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(playerBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		BlockFace direction = getPlayerFacing();
		Block attachBlock = playerBlock;
		Block targetBlock = attachBlock.getRelative(direction);

		Material material = targetBlock.getType();
		byte data = targetBlock.getData();

		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
		}

		int distance = 0;
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getRelative(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			castMessage("Can't bridge any further");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		BlockList bridgeBlocks = new BlockList();
		bridgeBlocks.add(targetBlock);
		targetBlock.setType(material);
		targetBlock.setData(data);

		castMessage("A bridge extends!");
		controller.addToUndoQueue(player, bridgeBlocks);
		controller.updateBlock(targetBlock);

		Location effectLocation = targetBlock.getLocation();	
		effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, material.getId());	
		//castMessage("Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());

		return SpellResult.SUCCESS;
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
}
