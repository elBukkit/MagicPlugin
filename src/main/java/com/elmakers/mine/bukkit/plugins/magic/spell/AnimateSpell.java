package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.SimulateBatch;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class AnimateSpell extends SimulateSpell 
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (parameters.getBoolean("animate", false))
		{
			return super.onCast(parameters);
		}
		
		Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		MaterialAndData targetMaterial = new MaterialAndData(targetBlock);
		BlockFace powerFace = SimulateBatch.findPowerLocation(targetBlock, targetMaterial);
		if (powerFace == null)
		{
			return SpellResult.NO_TARGET;
		}
		final Block powerBlock = targetBlock.getRelative(powerFace);
		final BlockList modifiedBlocks = new BlockList();
		modifiedBlocks.add(targetBlock);
		modifiedBlocks.add(powerBlock);
		
		if (!isDestructible(targetBlock) || !isDestructible(powerBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		boolean simCheckDestructible = parameters.getBoolean("sim_check_destructible", true);
		simCheckDestructible = parameters.getBoolean("scd", simCheckDestructible);
		
		String commandLine = "cast " + getKey() + " animate true target self cooldown 0 bu true m " + targetBlock.getType().name().toLowerCase() +
				" cd " + (simCheckDestructible ? "true" : "false");
		String commandName = parameters.getString("name", "Automata");
		commandName = commandName + " " + MaterialBrush.getMaterialName(targetMaterial);
		
		targetBlock.setType(Material.COMMAND);
		BlockState commandData = targetBlock.getState();
		if (commandData == null || !(commandData instanceof CommandBlock)) {
			return SpellResult.FAIL;
		}
			
		CommandBlock copyCommand = (CommandBlock)commandData;
		copyCommand.setCommand(commandLine);
		copyCommand.setName(commandName);
		copyCommand.update();
		
		controller.updateBlock(targetBlock);

		String message = getMessage("cast_broadcast").replace("$name", commandName);
		if (message.length() > 0) {
			int maxRange = parameters.getInt("target_max_range", 128);
			controller.sendToMages(message, targetBlock.getLocation(), maxRange);	
		}
		
		Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {

			public void run() {
				powerBlock.setType(Material.REDSTONE_BLOCK);
				controller.updateBlock(powerBlock);
				registerForUndo(modifiedBlocks);
			}
		}, 1);
		
		return SpellResult.CAST;
	}

}
