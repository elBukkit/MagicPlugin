package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;

public class ForceSpell extends Spell
{
    int itemMagnitude = 1;
    int entityMagnitude = 3;
    int maxAllDistance = 20;
    
    public void forceAll(int magnitude, boolean push)
    {
        List<Entity> entities = player.getWorld().getEntities();
        for (Entity entity : entities)
        {
            if (entity == player) continue;
            Location playerLocation = player.getLocation();
            
            if (getDistance(playerLocation, entity.getLocation()) > maxAllDistance) continue;
            
            forceEntity(entity, push);
            
        }
    }
    
    @Override
    public boolean onCast(String[] parameters)
    {
        boolean push = false;
        int magnitude = itemMagnitude;
        
        for (int i = 0; i < parameters.length; i++)
        {
            String parameter = parameters[i];

            if (parameter.equalsIgnoreCase("push"))
            {
                push = true;
                continue;
            }
            
            // try magnitude
            try
            {
                magnitude = Integer.parseInt(parameter);
                
                // Assume number, ok to continue
                continue;
            } 
            catch(NumberFormatException ex)
            {
            }
        }
        
        targetEntity(Entity.class);
        Target target = getTarget();

        if ((target == null || !target.isEntity() || target.isBlock()) && (getYRotation() < -60 || getYRotation() > 60))
        {
            if (push)
            {
                castMessage(player, "Get away!");
                forceAll(magnitude, true);
            }
            else
            {
                castMessage(player, "Gimme!");
                forceAll(magnitude, false);
            }
            return true;
        }
        
        if (target == null || !target.hasTarget() || !target.isEntity())
        {
            return false;
        }
           
        forceEntity(target.getEntity(), push);
        
        if (push)
        {
            castMessage(player, "Shove!");
        }
        else
        {
            castMessage(player, "Yoink!");
        }
        return true;
    }
    
    protected void forceEntity(Entity target, boolean push)
    {
        int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
        Location playerLocation = player.getLocation();
        Vector targetLoc = new Vector(target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ());
        Vector playerLoc = new Vector(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
        Vector aimVector = playerLoc;
        if (push)
        {
            aimVector = getAimVector();
        }
        else
        {
            aimVector.subtract(targetLoc);
        }
        aimVector.normalize();
        aimVector.multiply(magnitude);
        
        CraftEntity ce = (CraftEntity)target;
        ce.setVelocity(aimVector);
    }
}
