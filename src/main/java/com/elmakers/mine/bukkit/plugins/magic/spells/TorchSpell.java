package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TorchSpell extends BlockSpell 
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (parameters.containsKey("weather"))
		{
			String weatherString = parameters.getString("weather");
			World world = getLocation().getWorld();
			if (weatherString.equals("storm")) {
				world.setStorm(true);
				world.setThundering(true);
			} else {
				world.setStorm(false);
				world.setThundering(false);
			}
		}
		if (parameters.containsKey("time"))
		{
			long targetTime = 0;
			String typeString = parameters.getString("time", "day");
			String timeDescription = "day";
			if (typeString.equalsIgnoreCase("toggle")) {
				long currentTime = getTime();
				if (currentTime > 13000) {
					typeString = "day";
				} else {
					typeString = "night";
				}
			}
			
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
			setTime(targetTime);    
			castMessage("Changed time to " + timeDescription);
			return SpellResult.CAST;
		}

		
		boolean allowNight = parameters.getBoolean("allow_night", false);
		boolean allowDay = parameters.getBoolean("allow_day", false);
		if (getYRotation() > 80 && allowDay)
		{
			castMessage("FLAME ON!");
			setTime(0);
			return SpellResult.CAST;
		}


		if (getYRotation() < -80 && allowNight)
		{
			castMessage("FLAME OFF!");
			setTime(13000);
			return SpellResult.CAST;
		}

		Block target = getTargetBlock();	
		Block face = getLastBlock();

		if (target == null || face == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
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

		boolean allowLightstone = parameters.getBoolean("allow_glowstone", false);
		if 
		(
				face == null
				|| 		(!isAir && !isWater)
				||		(targetMaterial == Material.GLOWSTONE && !allowLightstone)
				)
		{
			castMessage("Can't put a torch there");
			return SpellResult.NO_TARGET;
		}

		if (!replaceAttachment)
		{
			target = face;
		}	

		castMessage("Flame on!");
		BlockList torchBlock = new BlockList();
		target.setType(targetMaterial);
		torchBlock.add(target);
		registerForUndo(torchBlock);
		controller.updateBlock(target);

		return SpellResult.CAST;
	}
}
