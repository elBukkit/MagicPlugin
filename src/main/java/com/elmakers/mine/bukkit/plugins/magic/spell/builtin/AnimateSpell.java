package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.SimulateBatch;
import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;

public class AnimateSpell extends SimulateSpell 
{
	public final static String[] ANIMATE_PARAMETERS = {
		"animate", "sim_check_destructible", "seed_radius", "restricted", "obworld", "btarget"
	};
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
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
		Set<Material> restricted = controller.getMaterialSet(parameters.getString("restricted", "restricted"));
		if (restricted.contains(targetMaterial.getMaterial()))
		{
			return SpellResult.RESTRICTED;
		}
		
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

		int seedRadius = parameters.getInt("seed_radius", 0);
		if (seedRadius > 0) {
			targetMaterial = ConfigurationUtils.getMaterialAndData(parameters, "material", targetMaterial);
			for (int dx = -seedRadius; dx < seedRadius; dx++) {
				for (int dz = -seedRadius; dz < seedRadius; dz++) {
					for (int dy = -seedRadius; dy < seedRadius; dy++) {
						Block seedBlock = targetBlock.getRelative(dx, dy, dz);
						if (isDestructible(seedBlock)) {
							targetMaterial.modify(seedBlock);
						}
					}
				}
			}
		}
		
		boolean simCheckDestructible = parameters.getBoolean("sim_check_destructible", true);
		simCheckDestructible = parameters.getBoolean("scd", simCheckDestructible);
		
		String commandLine = "cast " + getKey() + " animate true target self cooldown 0 bu true m " 
				+ targetMaterial.getKey() +
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
			controller.sendToMages(message, targetBlock.getLocation());	
		}
		
		Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {

			public void run() {
				powerBlock.setType(Material.REDSTONE_BLOCK);
				controller.updateBlock(powerBlock);
				registerForUndo(modifiedBlocks);
			}
		}, SimulateBatch.POWER_DELAY_TICKS + 1);
		
		return SpellResult.CAST;
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		super.getParameterOptions(examples, parameterKey);
		
		if (parameterKey.equals("animate") || parameterKey.equals("sim_check_destructible")) {
			examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
		}
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.addAll(Arrays.asList(ANIMATE_PARAMETERS));
	}
}
