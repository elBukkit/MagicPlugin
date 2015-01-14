package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.block.batch.SimpleBlockAction;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class FireSpell extends BlockSpell
{
	private final static int		DEFAULT_RADIUS	= 4;
	private final static int		DEFAULT_ELEMENTAL_DAMAGE = 5;
	private final static int		DEFAULT_ELEMENTAL_FIRE_TICKS = 200;
    
	public class FireAction extends SimpleBlockAction
	{
		public FireAction(Spell spell, UndoList undoList)
		{
			super(spell, undoList);
		}

		@Override
		public SpellResult perform(ConfigurationSection parameters, Block block)
		{
			if (block.getType() == Material.AIR || block.getType() == Material.FIRE || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
			{
				return SpellResult.NO_TARGET;
			}
            Material material = Material.FIRE;
			if (block.getType() == Material.ICE || block.getType() == Material.SNOW)
			{
				material = Material.AIR;
			}
			else
			{
				block = block.getRelative(BlockFace.UP);
			}
            if (block.getType() == Material.FIRE || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
            {
                return SpellResult.NO_TARGET;
            }
			super.perform(parameters, block);
            MaterialAndData applyMaterial = new MaterialAndData(material);
            applyMaterial.modify(block);

			return SpellResult.CAST;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		if (target == null || !target.isValid()) 
		{
			return SpellResult.NO_TARGET;
		}

		Block targetBlock = target.getBlock();
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		Entity entity = target.getEntity();
		if (entity != null && controller.isElemental(entity)) {
			controller.damageElemental(entity, 
					parameters.getDouble("elemental_damage", DEFAULT_ELEMENTAL_DAMAGE), 
					parameters.getInt("fire_ticks", DEFAULT_ELEMENTAL_FIRE_TICKS), mage.getCommandSender());
		}

        if (entity != null)
        {
            registerModified(entity);
        }

		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = (int)(mage.getRadiusMultiplier() * radius);
		
		FireAction action = new FireAction(this, getUndoList());

		if (radius < 1)
		{
			action.perform(parameters, findBlockUnder(targetBlock));
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}

		registerForUndo();

		return SpellResult.CAST;
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}
}
