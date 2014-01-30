package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class GotoSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		String playerName = parameters.getString("player");
		Player targetPlayer = null;
		if (playerName != null)
		{
			targetPlayer = controller.getPlugin().getServer().getPlayer(playerName);
		}

		targetEntity(Player.class);

		if (getYRotation() > 80)
		{
			Player destination = getFarthestPlayer(getPlayer());
			if (destination == null) return SpellResult.NO_TARGET;
			getPlayer().teleport(destination);
			castMessage("Teleporting you to " + destination.getName());
			return SpellResult.SUCCESS;
		}

		Target target = getTarget();
		Entity targetEntity = target.getEntity();

		if (targetEntity != null && targetEntity instanceof Player)
		{
			Player targetedPlayer = (Player)targetEntity;
			Player destination = getFarthestPlayer(targetedPlayer);
			if (destination == null) return SpellResult.NO_TARGET;
			targetedPlayer.teleport(destination);
			castMessage("Teleporting " + targetedPlayer.getName() + " to " + destination.getName());
			return SpellResult.SUCCESS;
		}

		Location destination = getPlayer().getLocation();
		if (target.isBlock())
		{
			destination = target.getLocation();
			destination.setY(destination.getY() + 1);
		}

		if (targetPlayer == null)
		{
			targetPlayer = getFarthestPlayer(getPlayer());
		}

		if (targetPlayer == null) return SpellResult.NO_TARGET;
		targetPlayer.teleport(destination);
		castMessage("Teleporting " + targetPlayer.getName() + " to your target");

		return SpellResult.SUCCESS;
	}

	protected Player getFarthestPlayer(Player fromPlayer)
	{
		Player destinationPlayer = null;
		List<Player> players = fromPlayer.getLocation().getWorld().getPlayers();
		double targetToDestinationDistance = 0;

		for (Player d : players)
		{
			if (d != fromPlayer)
			{
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
