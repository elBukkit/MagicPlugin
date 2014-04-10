package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.SimpleBlockAction;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FrostSpell extends BlockSpell
{
	private static final int			DEFAULT_RADIUS			= 2;
	private static final int            DEFAULT_TIME_TO_LIVE = 0;
	private static final int            DEFAULT_PLAYER_DAMAGE = 1;
	private static final int            DEFALT_ENTITY_DAMAGE = 10;
	private static final int			DEFAULT_SLOWNESS = 1;
	private static final int			DEFAULT_SLOWNESS_DURATION = 200;

	public class FrostAction extends SimpleBlockAction
	{
		private Material iceMaterial;
		
		public FrostAction(Material iceMaterial) {
			this.iceMaterial = iceMaterial;
		}
		
		@SuppressWarnings("deprecation")
		public SpellResult perform(Block block)
		{
			if (isTransparent(block.getType()))
			{
				return SpellResult.NO_TARGET;
			}
			Material material = Material.SNOW;
			if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
			{
				material = iceMaterial;
			}
			else if (block.getType() == Material.LAVA)
			{
				material = Material.COBBLESTONE;
			}
			else if (block.getType() == Material.STATIONARY_LAVA)
			{
				material = Material.OBSIDIAN;
			}
			else if (block.getType() == Material.FIRE)
			{
				material = Material.AIR;
			}
			else if (block.getType() == Material.SNOW)
			{
				material = Material.SNOW;
			}
			else
			{
				block = block.getRelative(BlockFace.UP);
			}
			super.perform(block);
			if (block.getType() == Material.SNOW && material == Material.SNOW) {
				if (block.getData() < 7) {
					block.setData((byte)(block.getData() + 1));
				}
			} else {
				block.setType(material);
			}
			return SpellResult.CAST;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();

		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}
		
		int playerDamage = parameters.getInteger("player_damage", DEFAULT_PLAYER_DAMAGE);
		int entityDamage = parameters.getInteger("entity_damage", DEFALT_ENTITY_DAMAGE);
		int defaultRadius = parameters.getInteger("radius", DEFAULT_RADIUS);
		int timeToLive = parameters.getInt("undo", DEFAULT_TIME_TO_LIVE);
		int slowness = parameters.getInt("slowness", DEFAULT_SLOWNESS);
		int slownessDuration = parameters.getInt("slowness_duration", DEFAULT_SLOWNESS_DURATION);
		Material iceMaterial = parameters.getMaterial("ice", Material.ICE);

		if (target.hasEntity())
		{
			Entity targetEntity = target.getEntity();
			if (targetEntity instanceof LivingEntity)
			{
				LivingEntity li = (LivingEntity)targetEntity;
				if (slowness > 0) {
					PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, slownessDuration, slowness, false);
					li.addPotionEffect(effect);
				}
				if (li instanceof Player)
				{
					li.damage(playerDamage, getPlayer());
				}
				else
				{
					li.damage(entityDamage, getPlayer());
				}
			}
		}
		
		if (!hasBuildPermission(target.getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int radius = parameters.getInt("radius", defaultRadius);
		radius = (int)(mage.getRadiusMultiplier() * radius);		
		FrostAction action = new FrostAction(iceMaterial);

		if (radius <= 1)
		{
			action.perform(target.getBlock());
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}


		BlockList frozenBlocks = action.getBlocks();
		frozenBlocks.setTimeToLive(timeToLive);
		registerForUndo(frozenBlocks);
		controller.updateBlock(target.getBlock());

		return SpellResult.CAST;
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}
}
