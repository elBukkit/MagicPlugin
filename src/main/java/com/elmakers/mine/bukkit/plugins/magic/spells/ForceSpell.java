package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;
import java.util.Map;

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
    Entity targetEntity = null;
    int maxRange = 64;
    
    public void forceAll(int magnitude, boolean push)
    {
        List<Entity> entities = player.getWorld().getEntities();
        for (Entity entity : entities)
        {
            if (entity == player) continue;
            Location playerLocation = player.getLocation();
            
            if (getDistance(playerLocation, entity.getLocation()) > maxAllDistance) continue;
            
            forceEntity(entity, playerLocation, !push);
        }
    }
    
    @Override
    public boolean onCast(Map<String, Object> parameters)
    {
        boolean push = false;
        boolean pull = false;
        int magnitude = itemMagnitude;
        
        setMaxRange(maxRange, true);
        
        if (targetEntity != null)
        {
            if (targetEntity instanceof LivingEntity)
            {
                LivingEntity le = (LivingEntity)targetEntity;
                if (le.isDead())
                {
                    targetEntity = null;
                }
            }
            if (targetEntity != null && getDistance(player.getLocation(), targetEntity.getLocation()) > maxRange)
            {
                targetEntity = null;
            }
        }
        
        if (parameters.containsKey("type"))
        {
            String typeString = (String)parameters.get("type");
            push = typeString.equals("push");
            pull = typeString.equals("pull");
        }
        
        if (parameters.containsKey("size"))
        {
            magnitude = (Integer)parameters.get("size");
        }
        
        targetEntity(Entity.class);
        Target target = getTarget();

        if 
        (
            (push || pull)
        &&  (target == null || !target.isEntity() || target.isBlock()) 
        &&  (getYRotation() < -60 || getYRotation() > 60)
        )
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
        
        if (target == null || !target.hasTarget())
        {
            targetEntity = null;
            return false;
        }
        
        if (target.isEntity())
        {
            Entity newEntity = target.getEntity();
            if 
            (
                targetEntity == null 
            ||  (newEntity instanceof LivingEntity) 
            ||  !(targetEntity instanceof LivingEntity)
            )
            {
                targetEntity = newEntity;
                if (!(push || pull)) return true;
            }
        }
        
        if (targetEntity == null)
        {
            return false;
        }
           
        Location destination = target.getLocation();
        if (pull) destination = player.getLocation();
        forceEntity(targetEntity, destination, push);
        
        if (pull)
        {
            castMessage(player, "Yoink!");
        }
        else
        {
            castMessage(player, "Shove!");
        }
        return true;
    }
    
    protected void forceEntity(Entity target, Location destination, boolean useAim)
    {
        int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
        Vector targetLoc = new Vector(target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ());
        Vector destinationLoc = new Vector(destination.getBlockX(), destination.getBlockY(), destination.getBlockZ());
        Vector forceVector = destinationLoc;
        if (useAim)
        {
            forceVector = getAimVector();
        }
        else
        {
            forceVector.subtract(targetLoc);                   
        }
        forceVector.normalize();
        forceVector.multiply(magnitude);
        
        CraftEntity ce = (CraftEntity)target;
        ce.setVelocity(forceVector);
    }
    
    @Override
    public void onCancel()
    {
        if (targetEntity != null)
        {
            if 
            (
                    (targetEntity instanceof LivingEntity) 
            &&      !targetEntity.isDead() 
            &&      getDistance(player.getLocation(), targetEntity.getLocation()) < maxRange
            )
            {
                player.sendMessage("Released target");
            }
            targetEntity = null;
        }
    }

}
