package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.Target;

public class ForceSpell extends Spell
{
    int defaultMagnitude = 3;
    int maxAllDistance = 50;
    
    public ForceSpell()
    {
        setCooldown(500);
        addVariant("push", Material.RAILS, getCategory(), "Push things away from you", "push");
    }
    
    public void forceAll(int magnitude, boolean push)
    {
        List<Entity> entities = player.getWorld().getEntities();
        for (Entity entity : entities)
        {
            if (entity == player) continue;
            Location playerLocation = player.getLocation();
            
            if (getDistance(playerLocation, entity.getLocation()) > maxAllDistance) continue;
            
            forceEntity(entity, magnitude, push);
            
        }
    }
    
    @Override
    public boolean onCast(String[] parameters)
    {
        boolean push = false;
        int magnitude = defaultMagnitude;
        
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
        
        if (getYRotation() < -80)
        {
            castMessage(player, "Get away!");
            forceAll(magnitude, true);
            return true;
        }
        
        if (getYRotation() > 80)
        {
           castMessage(player, "Gimme!");
           forceAll(magnitude, false);
           return true;
        }
        
        targetEntity(Entity.class);
        Target target = getTargetEntity();
        if (target == null || !target.hasTarget())
        {
            return false;
        }
           
        forceEntity(target.getEntity(), magnitude, push);
        
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
    
    protected void forceEntity(Entity target, int magnitude, boolean push)
    {
        Location playerLocation = player.getLocation();
        Vector targetLoc = new Vector(target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ());
        Vector playerLoc = new Vector(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
        Vector aimVector = playerLoc;
        if (push)
        {
            aimVector = new Vector((0 - Math.sin(Math.toRadians(playerLocation.getYaw()))), (0 - Math.sin(Math
                    .toRadians(playerLocation.getPitch() + 45))), Math.cos(Math.toRadians(playerLocation.getYaw())));
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

    @Override
    public String getName()
    {
        return "pull";
    }

    @Override
    public String getCategory()
    {
        return "help";
    }

    @Override
    public String getDescription()
    {
        return "Pull things toward you";
    }

    @Override
    public Material getMaterial()
    {
        return Material.FISHING_ROD;
    }

}
