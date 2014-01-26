package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public abstract class BlockSpell extends Spell {
	
	private Set<Material>	indestructible;
	private Set<Material>	destructible;
	private boolean checkDestructible = true;
	
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
		destructible = null;
		if (parameters.containsKey("destructible")) {
			destructible = parameters.getMaterials("destructible", "");
		}
		checkDestructible = parameters.getBoolean("check_destructible", checkDestructible);
	}
}
