package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.SimulateBatch;
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
		BlockFace powerFace = SimulateBatch.findPowerLocation(targetBlock, targetBlock.getType());
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
