package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;

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
		if (!mage.hasBuildPermission(getPlayer().getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		int size = parameters.getInt("size", defaultSize);
		size = (int)(mage.getRadiusMultiplier() * size);
		boolean useFire = parameters.getBoolean("fire", true);
		String projectileClass = parameters.getString("projectile", "Fireball");
		try {
			Location playerLocation = getPlayer().getLocation();
			Class<? extends Projectile> projectileType = (Class<? extends Projectile>)Class.forName("org.bukkit.entity." + projectileClass);
			Projectile projectile = getPlayer().launchProjectile(projectileType);
			projectile.setShooter(getPlayer());
			if (projectile instanceof WitherSkull) {
				playerLocation.getWorld().playSound(playerLocation, Sound.WITHER_SHOOT, 1.0f, 1.5f);		
			}
			if (projectile instanceof Fireball) {
				Fireball fireball = (Fireball)projectile;
				fireball.setIsIncendiary(useFire);
				fireball.setYield(size);
				if (!(projectile instanceof WitherSkull)) {
					playerLocation.getWorld().playSound(playerLocation, Sound.GHAST_FIREBALL, 1.0f, 1.5f);
				}
			}
		} catch(Exception ex) {
			sendMessage("Unknown projectile class " + projectileClass);
			return SpellResult.FAILURE;
		}
		return SpellResult.SUCCESS;
	}
}
