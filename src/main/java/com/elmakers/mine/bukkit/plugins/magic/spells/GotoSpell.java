package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class GotoSpell extends Spell
{
    @Override
    public boolean onCast(ConfigurationNode parameters) 
    {
        String playerName = parameters.getString("player");
        Player targetPlayer = null;
        if (playerName != null)
        {
            targetPlayer = spells.getPlugin().getServer().getPlayer(playerName);
        }
        
        targetEntity(Player.class);
        
        if (getYRotation() > 80)
        {
            Player destination = getFarthestPlayer(player);
            if (destination == null) return false;
            player.teleport(destination);
            castMessage(player, "Teleporting you to " + destination.getName());
            return true;
        }
        
        Target target = getTarget();
        Entity targetEntity = target.getEntity();
        
        if (targetEntity != null && targetEntity instanceof Player)
        {
            Player targetedPlayer = (Player)targetEntity;
            Player destination = getFarthestPlayer(targetedPlayer);
            if (destination == null) return false;
            targetedPlayer.teleport(destination);
            castMessage(player, "Teleporting " + targetedPlayer.getName() + " to " + destination.getName());
            return true;
        }
        
        Location destination = player.getLocation();
        if (target.isBlock())
        {
            destination = target.getLocation();
            destination.setY(destination.getY() + 1);
        }
        
        if (targetPlayer == null)
        {
            targetPlayer = getFarthestPlayer(player);
        }
        
        if (targetPlayer == null) return false;
        targetPlayer.teleport(destination);
        castMessage(player, "Teleporting " + targetPlayer.getName() + " to your target");
        
        return true;
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
                double dd = getDistance(d.getLocation(), fromPlayer.getLocation());
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
