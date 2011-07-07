package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.Target;

public class GotoSpell extends Spell
{

    @Override
    public boolean onCast(String[] parameters)
    {
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
            Player targetPlayer = (Player)targetEntity;
            Player destination = getFarthestPlayer(targetPlayer);
            if (destination == null) return false;
            targetPlayer.teleport(destination);
            castMessage(player, "Teleporting " + targetPlayer.getName() + " to " + destination.getName());
            return true;
        }
        
        Location destination = player.getLocation();
        if (target.isBlock())
        {
            destination = target.getLocation();
            destination.setY(destination.getY() + 1);
        }
        
        Player targetPlayer = getFarthestPlayer(player);
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

    @Override
    public String getName()
    {
        return "gather";
    }

    @Override
    public String getCategory()
    {
        return "exploration";
    }

    @Override
    public String getDescription()
    {
        return "Gather groups of players together";
    }

    @Override
    public Material getMaterial()
    {
        return Material.GLOWSTONE_DUST;
    }

}
