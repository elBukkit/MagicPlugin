package com.elmakers.mine.bukkit.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDeathEvent;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.magic.SpellEventType;
import com.elmakers.mine.bukkit.persistence.dao.LocationData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class RecallSpell extends Spell
{
    private boolean      autoDropOnDeath     = true;
    private boolean      autoSpawn           = true;
    private final int    disableDistance     = 5;
    private LocationData marker              = null;

    @Override
    public String getDescription()
    {
        return "Marks locations and returns you to them";
    }

    @Override
    public String getName()
    {
        return "recall";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        boolean setMarker = parameters.hasFlag("set");
        
        if (parameters.hasFlag("spawn"))
        {
            castMessage(player, "Returning you home");
            player.teleport(player.getWorld().getSpawnLocation());
            return true;
        }

        if (!setMarker)
        {
            if (marker == null && autoSpawn)
            {
                castMessage(player, "Returning you home");
                player.teleport(player.getWorld().getSpawnLocation());
                return placeMarker(targeting.getPlayerBlock());
            }
            else
            {
                if (marker == null)
                {
                    sendMessage(player, "No recall marker set");
                    return false;
                }

                Location markerLocation = marker.getLocation();
                double distance = targeting.getDistance(player.getLocation(), markerLocation);

                if (distance < disableDistance && autoSpawn)
                {
                    castMessage(player, "Returning you home");
                    player.teleport(player.getWorld().getSpawnLocation());
                    
                }
                else
                {
                    castMessage(player, "Returning you to your marker");
                    player.teleport(markerLocation);
                }
            }
            return true;
        }

        if (marker == null)
        {
            return placeMarker(targeting.getTargetBlock());
        }

        Location markerLocation = marker.getLocation();
        double distance = targeting.getDistance(player.getLocation(),  markerLocation);

        if (distance < disableDistance)
        {
            marker = null;
            castMessage(player, "You dispell your marker");
            return true;
        }

        return placeMarker(targeting.getTargetBlock());
    }

    @Override
    public void onLoad()
    {
        //autoDropOnDeath = properties.getBoolean("spells-recall-auto-resurrect", autoDropOnDeath);
        //autoSpawn = properties.getBoolean("spells-recall-auto-spawn", autoSpawn);
 
        if (autoDropOnDeath)
        {
            magic.registerEvent(SpellEventType.PLAYER_DEATH, this);
        }
    }

    @Override
    public void onPlayerDeath(EntityDeathEvent event)
    {
        if (autoDropOnDeath && hasSpellPermission(player) && marker == null)
        {
            placeMarker(targeting.getPlayerBlock());
        }
    }

    protected boolean placeMarker(Block target)
    {
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        Block targetBlock = target.getFace(BlockFace.UP);
        if (targetBlock.getType() != Material.AIR)
        {
            targetBlock = targeting.getFaceBlock();
        }
        if (targetBlock.getType() != Material.AIR)
        {
            castMessage(player, "Can't place a marker there");
            return false;
        }

        if (marker != null)
        {
            castMessage(player, "You move your recall marker");
        }
        else
        {
            castMessage(player, "You place a recall marker");
        }

        marker = new LocationData(player.getLocation());
        marker.setX(targetBlock.getX());
        marker.setY(targetBlock.getY());
        marker.setZ(targetBlock.getZ());

        return true;
    }

}
