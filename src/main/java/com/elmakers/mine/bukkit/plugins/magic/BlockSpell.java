package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

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
	protected void processParameters(ConfigurationNode parameters) {
		super.processParameters(parameters);
		indestructible = null;
		if (parameters.containsKey("indestructible")) {
			indestructible = parameters.getMaterials("indestructible", "");
		}
		if (parameters.containsKey("id")) {
			indestructible = parameters.getMaterials("id", "");
		}
		destructible = null;
		if (parameters.containsKey("destructible")) {
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
}
