package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.block.batch.SimpleBlockAction;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class LightningSpell extends UndoableSpell
{
	public class ShockAction extends SimpleBlockAction
	{
		protected double density;
		protected int    thunderThreshold;

		public ShockAction(Spell spell, UndoList undoList, double density, int thunderThreshold)
		{
			super(spell, undoList);
			this.density = density;
			this.thunderThreshold = thunderThreshold;
		}

		public SpellResult perform(ConfigurationSection parameters, Block block)
		{
			if (Math.random() > density) return SpellResult.NO_ACTION;

			super.perform(parameters, block);
			World world = block.getWorld();
			world.strikeLightning(block.getLocation());
			
			// TODO: Make this a special parameter
			/*
			if (blocks.size() > thunderThreshold)
			{
				world.setThundering(true);
			}
			*/

			return SpellResult.CAST;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}

		int radius = parameters.getInt("radius", 1);
		radius = (int)(mage.getRadiusMultiplier() * radius);	

		double ratio = (radius < 2) ? 1.0 : (radius < 4) ? 0.5 : 0.25;
		ShockAction action = new ShockAction(this, null, ratio, 5);

		if (radius <= 1)
		{
			action.perform(parameters, target.getBlock());
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}
		
		registerForUndo();

		return SpellResult.CAST;
	}
}
