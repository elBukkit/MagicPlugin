package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ProjectileSpell extends Spell 
{
	int defaultSize = 1;

	@SuppressWarnings("unchecked")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		size = (int)(playerSpells.getPowerMultiplier() * size);
		boolean useFire = parameters.getBoolean("fire", true);
		String projectileClass = parameters.getString("projectile", "Fireball");
		try {
			Class<? extends Projectile> projectileType = (Class<? extends Projectile>)Class.forName("org.bukkit.entity." + projectileClass);
			Projectile projectile = player.launchProjectile(projectileType);
			projectile.setShooter(player);
			if (projectile instanceof Fireball) {
				Fireball fireball = (Fireball)projectile;
				fireball.setIsIncendiary(useFire);
				fireball.setYield(size);
			}
		} catch(Exception ex) {
			sendMessage("Unknown projectile class " + projectileClass);
			return SpellResult.FAILURE;
		}
		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoadTemplate(ConfigurationNode node)
	{
		disableTargeting();
	}
}
