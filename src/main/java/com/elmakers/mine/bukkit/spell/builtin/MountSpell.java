package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class MountSpell extends TargetingSpell {

	@Override
	public SpellResult onCast(ConfigurationSection parameters) {
		Player player = getPlayer();
		if (player == null) {
			return SpellResult.PLAYER_REQUIRED;
		}
		Entity current = player.getVehicle();
		
		// Make it so this spell can be used to get someone off of you
		player.eject();
		if (current != null) {
			current.eject();
		}
		Entity targetEntity = getTarget().getEntity();
		if (targetEntity == null) {
			return SpellResult.NO_TARGET;
		}
		
		targetEntity.setPassenger(getPlayer());
		
		return SpellResult.CAST;
	}

}
