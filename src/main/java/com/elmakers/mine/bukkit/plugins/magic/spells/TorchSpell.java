package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TorchSpell extends Spell 
{
	private boolean allowDay = true;
	private boolean allowNight = true;
	private boolean allowLightstone = true;
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
	    if (parameters.containsKey("time"))
	    {
	        long targetTime = 0;
	        String typeString = parameters.getString("time", "day");
	        String timeDescription = "day";
            if (typeString.equalsIgnoreCase("night"))
            {
                targetTime = 13000;
                timeDescription = "night";
            }
            else
            {
                try 
                {
                    targetTime = Long.parseLong(typeString);
                    timeDescription = "raw: " + targetTime;
                } 
                catch (NumberFormatException ex) 
                {
                    targetTime = 0;
                }
            }
            setRelativeTime(targetTime);    
            castMessage(player, "Changed time to " + timeDescription);
            return true;
	    }
		
		if (getYRotation() > 80 && allowDay)
		{
			castMessage(player, "FLAME ON!");
			setRelativeTime(0);
			return true;
		}
		
		
		if (getYRotation() < -80 && allowNight)
		{
			castMessage(player, "FLAME OFF!");
			setRelativeTime(13000);
			return true;
		}
		
		Block target = getTargetBlock();	
		Block face = getLastBlock();
		
		if (target == null || face == null)
		{
			castMessage(player, "No target");
			return false;
		}
		
		boolean isAir = face.getType() == Material.AIR;
		boolean isAttachmentSlippery = target.getType() == Material.GLASS || target.getType() == Material.ICE;
		boolean replaceAttachment = target.getType() == Material.SNOW || target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
		boolean isWater = face.getType() == Material.STATIONARY_WATER || face.getType() == Material.WATER;
		boolean isNether = target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
		Material targetMaterial = Material.TORCH;
		
		if (isWater || isAttachmentSlippery || isNether)
		{
			targetMaterial = Material.GLOWSTONE;
			replaceAttachment = true;
		}
		
		if 
		(
				face == null
		|| 		(!isAir && !isWater)
		||		(targetMaterial == Material.GLOWSTONE && !allowLightstone)
		)
		{
			player.sendMessage("Can't put a torch there");
			return false;
		}

		if (!replaceAttachment)
		{
			target = face;
		}	
		
		castMessage(player, "Flame on!");
		BlockList torchBlock = new BlockList();
		target.setType(targetMaterial);
		torchBlock.add(target);
		spells.addToUndoQueue(player, torchBlock);
		
		return true;
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		allowNight = properties.getBoolean("allow_night", allowNight);
		allowDay = properties.getBoolean("allow_day", allowDay);
		allowLightstone = properties.getBoolean("allow_glowstone", allowLightstone);
	}
}
