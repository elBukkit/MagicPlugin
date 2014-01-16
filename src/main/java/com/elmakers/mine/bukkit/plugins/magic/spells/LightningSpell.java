package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LightningSpell extends Spell
{
	public class ShockAction extends SimpleBlockAction
	{
		protected double density;
		protected int    thunderThreshold;
		protected Player player;

		public ShockAction(Player player, double density, int thunderThreshold)
		{
			this.player = player;
			this.density = density;
			this.thunderThreshold = thunderThreshold;
		}

		public SpellResult perform(Block block)
		{
			if (Math.random() > density) return SpellResult.COST_FREE;

			World world = player.getWorld();
			world.strikeLightning(block.getLocation());
			super.perform(block);
			if (blocks.size() > thunderThreshold)
			{
				world.setThundering(true);
			}

			return SpellResult.SUCCESS;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		int radius = parameters.getInt("radius", 1);
		radius = (int)(mage.getRadiusMultiplier() * radius);	

		double ratio = (radius < 2) ? 1.0 : (radius < 4) ? 0.5 : 0.25;
		ShockAction action = new ShockAction(getPlayer(), ratio, 5);

		if (radius <= 1)
		{
			action.perform(target.getBlock());
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}

		controller.addToUndoQueue(getPlayer(), action.getBlocks());
		castMessage("Zapped " + action.getBlocks().size() + " blocks");

		return SpellResult.SUCCESS;
	}
}
