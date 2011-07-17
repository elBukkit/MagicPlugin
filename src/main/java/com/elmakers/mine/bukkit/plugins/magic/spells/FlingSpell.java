package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.EntityPlayer;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;

public class FlingSpell extends Spell
{
	private final long safetyLength = 20000;
	private long lastFling = 0;
	
    protected int maxSpeedAtElevation = 32;
    protected double minMagnitude = 1.5;
    protected double maxMagnitude = 12; 

	@SuppressWarnings("unchecked")
    @Override
	public boolean onCast(ConfigurationNode parameters) 
	{
	    int height = 0;
	    Block playerBlock = player.getLocation().getBlock();
	    	    
	    // testing out a perf hack- don't send chunks while flinging!
        CraftPlayer cp = (CraftPlayer)player;
        EntityPlayer ep = cp.getHandle();
	    Chunk chunk = playerBlock.getChunk();
	    ep.chunkCoordIntPairQueue.clear();
	    ep.chunkCoordIntPairQueue.add(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
	    
	    while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
	    {
	        playerBlock = playerBlock.getFace(BlockFace.DOWN);
	        height++;
	    }
	    
	    double magnitude = (minMagnitude + (((double)maxMagnitude - minMagnitude) * ((double)height / maxSpeedAtElevation)));
      
		Vector velocity = getAimVector();
		
		if (player.getLocation().getBlockY() >= 128)
		{
		    velocity.setY(0);
		}
		
		velocity.multiply(magnitude);
		CraftPlayer craftPlayer = (CraftPlayer)player;
		craftPlayer.setVelocity(velocity);
		castMessage(player, "Whee!");
		
        spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
        lastFling = System.currentTimeMillis();
		return true;
	}
	
	@Override
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getCause() != DamageCause.FALL) return;

        spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);
        
        if (lastFling == 0) return;
        
        if (lastFling + safetyLength > System.currentTimeMillis())
        {
            event.setCancelled(true);
            lastFling = 0;
        }
    }
}
