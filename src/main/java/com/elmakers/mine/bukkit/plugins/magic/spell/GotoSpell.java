package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;

public class GotoSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		if (getYRotation() > 80)
		{
			Player destination = getFarthestPlayer(getPlayer());
			if (destination == null) return SpellResult.NO_TARGET;
			getPlayer().teleport(destination);
			castMessage(getMessage("cast_to_player").replace("$to_player", destination.getName()));
			return SpellResult.CAST;
		}

		Target target = getTarget();
		Entity targetEntity = target.getEntity();

		if (targetEntity != null && targetEntity instanceof Player)
		{
			Player targetedPlayer = (Player)targetEntity;
			Player destination = getFarthestPlayer(targetedPlayer);
			if (destination == null) return SpellResult.NO_TARGET;
			targetedPlayer.teleport(destination);
			castMessage(getMessage("cast_player_to_player").replace("$from_player", targetedPlayer.getName().replace("$to_player", destination.getName())));
			return SpellResult.CAST;
		}

		Location destination = getPlayer().getLocation();
		if (target.isValid())
		{
			destination = target.getLocation();
			destination.setY(destination.getY() + 1);
		}
		
		Player targetPlayer = getFarthestPlayer(getPlayer());

		if (targetPlayer == null) return SpellResult.NO_TARGET;
		targetPlayer.teleport(destination);
		castMessage(getMessage("cast_player_to_target").replace("$from_player", targetPlayer.getName()));

		return SpellResult.CAST;
	}

	protected Player getFarthestPlayer(Player fromPlayer)
	{
		Player destinationPlayer = null;
		List<Player> players = fromPlayer.getLocation().getWorld().getPlayers();
		double targetToDestinationDistance = 0;

		for (Player d : players)
		{
			if (d.hasMetadata("NPC")) continue;
			if (d != fromPlayer)
			{
				Mage targetMage = controller.getMage((Player)d);
				// Check for protected players (admins, generally...)
				if (!mage.isSuperPowered() && targetMage.isSuperProtected()) {
					continue;
				}
				
				double dd = d.getLocation().distanceSquared(fromPlayer.getLocation());
				if (destinationPlayer == null || dd > targetToDestinationDistance)
				{
					targetToDestinationDistance = dd;
					destinationPlayer = d;
				}
			}
		}

		return destinationPlayer;
	}
}
