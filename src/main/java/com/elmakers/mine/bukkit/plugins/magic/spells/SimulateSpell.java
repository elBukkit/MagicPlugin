package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;

import com.elmakers.mine.bukkit.blocks.SimulateBatch;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class SimulateSpell extends BlockSpell {
	
	private static final int DEFAULT_RADIUS = 32;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) {
		Target t = getTarget();
		if (t == null) {
			return SpellResult.NO_TARGET;
		}
		
		Block target = t.getBlock();
		if (target == null) {
			return SpellResult.NO_TARGET;
		}
		
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = parameters.getInt("r", radius);
		
		Material birthMaterial = target.getType();
		birthMaterial = parameters.getMaterial("material", birthMaterial);
		Material deathMaterial = parameters.getMaterial("death_material", Material.AIR);

		SimulateBatch batch = new SimulateBatch(this, target.getLocation(), radius, birthMaterial, deathMaterial);
		
		boolean includeCommands = parameters.getBoolean("commands", true);
		if (includeCommands) {
			if (mage.getCommandSender() instanceof BlockCommandSender) {
				BlockCommandSender commandBlock = (BlockCommandSender)mage.getCommandSender();
				batch.setCommandBlock(commandBlock.getBlock());
			} else if (target.getType() == Material.COMMAND) {
				batch.setCommandBlock(target);
			}
		}
		
		mage.addPendingBlockBatch(batch);
		
		return SpellResult.CAST;
	}
}
