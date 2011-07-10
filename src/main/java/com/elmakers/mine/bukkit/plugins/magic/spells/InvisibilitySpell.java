package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;

public class InvisibilitySpell extends Spell
{
    public InvisibilitySpell()
    {
        addVariant("decoy", Material.CAKE, getCategory(), "Create a decoy of yourself", "decoy");
    }
    
    public class InvisiblePlayer
    {
        protected Player player;
        protected ArrayList<Player> cloaked = new ArrayList<Player>();
        protected boolean decoy = false;
        protected Location location;
        
        public InvisiblePlayer(Player player)
        {
            this.player = player;
            this.location = player.getLocation();
        }
        
        public boolean hasMoved()
        {
            Location current = player.getLocation();
            
            return (current.getBlockX() != location.getBlockX() || current.getBlockY() != location.getBlockY() || current.getBlockZ() != location.getBlockZ());
        }
        
        public void decoy()
        {
            uncloak();
            decoy = true;
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
                    if (decoy)
                    {
                        uncloakFrom(other);
                    }
                    else
                    {
                        cloakFrom(other);
                    }
                }
            }
            
        }
        
        public void uncloak()
        {
            for (Player other : cloaked)
            {
                if (!decoy)
                {
                    uncloakFrom(other);
                }
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
    };
    
    private HashMap<String, InvisiblePlayer> invisiblePlayers = new HashMap<String, InvisiblePlayer>();

    @Override
    public boolean onCast(String[] parameters)
    {
        InvisiblePlayer iPlayer = new InvisiblePlayer(player);
        invisiblePlayers.put(player.getName(), iPlayer);
        
        if (parameters.length > 0 && parameters[0].equals("decoy"))
        {
            iPlayer.decoy();
        }
        else
        {
            iPlayer.cloak();
        }
        checkListener();
        return true;
    }

    @Override
    public String getName()
    {
        return "cloak";
    }

    @Override
    public String getCategory()
    {
        return "stealth";
    }

    @Override
    public String getDescription()
    {
        return "Make yourself invisible until you move again";
    }

    @Override
    public Material getMaterial()
    {
        return Material.CHAINMAIL_CHESTPLATE;
    }

    protected void checkListener()
    {
        if (invisiblePlayers.size() == 0)
        {
            spells.unregisterEvent(SpellEventType.PLAYER_MOVE, this);
        }
        else
        {
            spells.registerEvent(SpellEventType.PLAYER_MOVE, this);
        }
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        InvisiblePlayer iPlayer = invisiblePlayers.get(player.getName());
        if (iPlayer == null) return;
        if (!iPlayer.hasMoved()) return;
        
        iPlayer.uncloak();
        invisiblePlayers.remove(player.getName());
        checkListener();
    }
}
