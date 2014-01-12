package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TorchSpell extends Spell 
{
	private boolean allowDay = false;
	private boolean allowNight = false;
	private boolean allowLightstone = false;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
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
			return SpellResult.SUCCESS;
		}

		if (getYRotation() > 80 && allowDay)
		{
			castMessage("FLAME ON!");
			setTime(0);
			return SpellResult.SUCCESS;
		}


		if (getYRotation() < -80 && allowNight)
		{
			castMessage("FLAME OFF!");
			setTime(13000);
			return SpellResult.SUCCESS;
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
		spells.addToUndoQueue(player, torchBlock);
		spells.updateBlock(target);

		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoadTemplate(ConfigurationNode properties)  
	{
		allowNight = properties.getBoolean("allow_night", allowNight);
		allowDay = properties.getBoolean("allow_day", allowDay);
		allowLightstone = properties.getBoolean("allow_glowstone", allowLightstone);
	}
}
