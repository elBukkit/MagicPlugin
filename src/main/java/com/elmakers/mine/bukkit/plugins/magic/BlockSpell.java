package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockList;

public abstract class BlockSpell extends Spell {
	
	private Set<Material>	indestructible;
	private Set<Material>	destructible;
	private boolean checkDestructible = true;
	private boolean bypassUndo				= false;
	private int modifiedBlocks = 0;
	
	public final static String[] BLOCK_PARAMETERS = {
		"indestructible", "destructible", "check_destructible", "bypass_undo"
	};
	
	private boolean isIndestructible(Block block)
	{
		if (indestructible == null) {
			return mage.isIndestructible(block);
		}
		return indestructible.contains(block.getType()) || mage.isIndestructible(block);
	}
	
	public boolean isDestructible(Block block)
	{
		if (isIndestructible(block)) return false;
		
		if (!checkDestructible) return true;
		if (destructible == null) {
			return mage.isDestructible(block);
		}
		return destructible.contains(block.getType());
	}
	
	protected void setDestructible(Set<Material> materials) {
		checkDestructible = true;
		destructible = materials;
	}

	@Override
	protected void processParameters(ConfigurationSection parameters) {
		super.processParameters(parameters);
		indestructible = null;
		if (parameters.contains("indestructible")) {
			indestructible = controller.getMaterialSet(parameters.getString("indestructible"));
		}
		if (parameters.contains("id")) {
			indestructible = controller.getMaterialSet(parameters.getString("id"));
		}
		destructible = null;
		if (parameters.contains("destructible")) {
			destructible = controller.getMaterialSet(parameters.getString("destructible"));
		}
		checkDestructible = parameters.getBoolean("check_destructible", true);
		checkDestructible = parameters.getBoolean("cd", checkDestructible);
		bypassUndo = parameters.getBoolean("bypass_undo", false);
		bypassUndo = parameters.getBoolean("bu", bypassUndo);
	}
	
	public void registerForUndo(BlockList list)
	{
		modifiedBlocks += list.size();
		if (!bypassUndo) {
			mage.registerForUndo(list);
		}
	}
	
	@Override
	protected void preCast()
	{
		modifiedBlocks = 0;
	}
	
	@Override
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		return message.replace("$count", Integer.toString(modifiedBlocks));
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.addAll(Arrays.asList(BLOCK_PARAMETERS));
	}
	
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		super.getParameterOptions(examples, parameterKey);
		
		if (parameterKey.equals("indestructible") || parameterKey.equals("destructible")) {
			examples.addAll(controller.getMaterialSets());
		} else if (parameterKey.equals("check_destructible") || parameterKey.equals("bypass_undo")) {
			examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
		}
	}
}
