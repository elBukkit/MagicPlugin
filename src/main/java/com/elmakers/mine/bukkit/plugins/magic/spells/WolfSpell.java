package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;

public class WolfSpell extends Spell
{
    private static final int maxWolves = 5;
    protected List<CraftWolf> wolves = new ArrayList<CraftWolf>();
    
	public CraftWolf newWolf(Target target)
	{
		if (target == null)
		{
			castMessage(player, "No target");
			return null;
		}
		
		Block targetBlock = target.getBlock();
        targetBlock = targetBlock.getFace(BlockFace.UP);
		if (target.isEntity())
        {      
            targetBlock = targetBlock.getFace(BlockFace.SOUTH);
        }
		
		CraftWolf entity = (CraftWolf)player.getWorld().spawnCreature(targetBlock.getLocation(), CreatureType.WOLF);
		if (entity == null)
		{
			sendMessage(player, "Your wolfie is DOA");
			return null;
		}
		tameWolf(entity);
		castMessage(player, "You summon a wolfie!");
		return entity;
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
	    this.targetEntity(LivingEntity.class);
		Target target = getTarget();
		if (target == null)
		{
			return false;
		}
		
		ArrayList<CraftWolf> newWolves = new ArrayList<CraftWolf>();
		
	    for (CraftWolf wolf : wolves)
	    {
	        if (!wolf.isDead())
	        {
	            newWolves.add(wolf);
	        }
	    }
	    
	    wolves = newWolves;
		
		if (wolves.size() >= maxWolves) 
		{
		   CraftWolf killWolf = wolves.remove(0);
		   killWolf.setHealth(0);
		}
		
    	CraftWolf wolf = newWolf(target);
        if (wolf == null)
        {
            return false;
        }
         
        wolves.add(wolf);
		
		Entity e = target.getEntity();
        if (e != null && e instanceof LivingEntity)
        {
            LivingEntity targetEntity = (LivingEntity)e;
            for (CraftWolf w : wolves)
            {
                w.setTarget(targetEntity);
            }
        }
		
		return true;
	}
	
	protected void tameWolf(CraftWolf wolfie)
	{
	    wolfie.setAngry(false);
        wolfie.setHealth(20);
        wolfie.setTamed(true);
        wolfie.setOwner(player);
	}
}
