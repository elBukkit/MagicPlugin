package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;

public class InvisibilitySpell extends Spell
{
    protected ArrayList<Player> cloaked = new ArrayList<Player>();
    protected Location location;

    @Override
    public boolean onCast(Map<String, Object> parameters)
    {
        this.location = player.getLocation();
       
        if (parameters.containsKey("type"))
        {
            String typeString = (String)parameters.get("type");
            if (typeString.equals("decoy"))
            {
                decoy();
            }
        }
       
        
        cloak();
        
        spells.registerEvent(SpellEventType.PLAYER_MOVE, this);
        return true;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!hasMoved()) return;
        
        uncloak();
        spells.unregisterEvent(SpellEventType.PLAYER_MOVE, this);
    }

    public boolean hasMoved()
    {
        Location current = player.getLocation();
        
        return (current.getBlockX() != location.getBlockX() || current.getBlockY() != location.getBlockY() || current.getBlockZ() != location.getBlockZ());
    }
    
    public void decoy()
    {
        uncloak();
        cloak();
    }
    
    public void cloak()
    {
        uncloak();
        
        List<Player> others = player.getWorld().getPlayers();
        for (Player other : others)
        {
            if (other != player && inRange(other))
            {
                cloaked.add(other);
                cloakFrom(other);
            }
        }
        
    }
    
    public void uncloak()
    {
        for (Player other : cloaked)
        {
            uncloakFrom(other); 
        }
        
        cloaked.clear();
    }
    
    protected boolean inRange(Player other) 
    {
        Location playerLocation = player.getLocation();
        Location otherLocation = other.getLocation();
        
        // hide from players under 256 blocks away
        int maxDistance = 256;
        return 
        (
            Math.pow(playerLocation.getX() - otherLocation.getX(), 2) 
        +   Math.pow(playerLocation.getY() - otherLocation.getY(), 2)
        +   Math.pow(playerLocation.getZ() - otherLocation.getZ(), 2)
        ) < maxDistance * maxDistance;
    }
    
    protected void cloakFrom(Player other)
    {
        CraftPlayer hide = (CraftPlayer)player;
        CraftPlayer hideFrom = (CraftPlayer)other;
        
        EntityPlayer fromEntity = hideFrom.getHandle();
        
        fromEntity.netServerHandler.sendPacket(new Packet29DestroyEntity(hide.getEntityId()));        
    }
    
    protected void uncloakFrom(Player other)
    {
        CraftPlayer hide = (CraftPlayer)player;
        CraftPlayer hideFrom = (CraftPlayer)other;
        
        EntityPlayer hideEntity = hide.getHandle();
        EntityPlayer fromEntity = hideFrom.getHandle();
        
        fromEntity.netServerHandler.sendPacket(new Packet20NamedEntitySpawn(hideEntity));
    }
}
