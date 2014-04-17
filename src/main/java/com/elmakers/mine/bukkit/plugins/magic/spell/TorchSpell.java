package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;

public class TorchSpell extends BlockSpell 
{
	private String timeType = "day";
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		World world = getWorld();
		if (world == null) {
			return SpellResult.NO_TARGET;
		}
		if (parameters.contains("weather"))
		{
			String weatherString = parameters.getString("weather");
			if (weatherString.equals("storm")) {
				world.setStorm(true);
				world.setThundering(true);
			} else {
				world.setStorm(false);
				world.setThundering(false);
			}
		}
		if (parameters.contains("time"))
		{
			long targetTime = 0;
			timeType = parameters.getString("time", "day");
			if (timeType.equalsIgnoreCase("toggle")) {
				long currentTime = world.getTime();
				if (currentTime > 13000) {
					timeType = "day";
				} else {
					timeType = "night";
				}
			}
			
			if (timeType.equalsIgnoreCase("night"))
			{
				targetTime = 13000;
			}
			else
			{
				try 
				{
					targetTime = Long.parseLong(timeType);
					timeType = "raw(" + targetTime + ")";
				} 
				catch (NumberFormatException ex) 
				{
					targetTime = 0;
				}
			}
			world.setTime(targetTime);
			return SpellResult.AREA;
		}
		
		boolean allowNight = parameters.getBoolean("allow_night", false);
		boolean allowDay = parameters.getBoolean("allow_day", false);
		if (isLookingUp() && allowDay)
		{
			timeType = "day";
			world.setTime(0);
			return SpellResult.AREA;
		}


		if (isLookingDown() && allowNight)
		{
			timeType = "night";
			world.setTime(13000);
			return SpellResult.AREA;
		}

		Block target = getTargetBlock();	
		Block face = getLastBlock();

		if (target == null || face == null)
		{
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
			return SpellResult.NO_TARGET;
		}

		if (!replaceAttachment)
		{
			target = face;
		}	

		BlockList torchBlock = new BlockList();
		target.setType(targetMaterial);
		torchBlock.add(target);
		registerForUndo(torchBlock);
		controller.updateBlock(target);

		return SpellResult.CAST;
	}
	
	@Override
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		return message.replace("$time", timeType);
	}
}
