package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class FreezeAction extends BaseSpellAction implements BlockAction
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		boolean freezeWater = parameters.getBoolean("freeze_water", true);
		boolean freezeLava = parameters.getBoolean("freeze_lava", true);
		boolean freezeFire = parameters.getBoolean("freeze_fire", true);
		Material iceMaterial = ConfigurationUtils.getMaterial(parameters, "ice", Material.ICE);

		Material material = Material.SNOW;
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			if (!freezeWater)
			{
				return SpellResult.NO_TARGET;
			}
			material = iceMaterial;
		}
		else if (block.getType() == Material.LAVA)
		{
			if (!freezeLava)
			{
				return SpellResult.NO_TARGET;
			}
			material = Material.COBBLESTONE;
		}
		else if (block.getType() == Material.STATIONARY_LAVA)
		{
			if (!freezeLava)
			{
				return SpellResult.NO_TARGET;
			}
			material = Material.OBSIDIAN;
		}
		else if (block.getType() == Material.FIRE)
		{
			if (!freezeFire)
			{
				return SpellResult.NO_TARGET;
			}
			material = Material.AIR;
		}
		else if (block.getType() == Material.SNOW)
		{
			material = Material.SNOW;
		}
		else
		{
			block = block.getRelative(BlockFace.UP);
		}
		updateBlock(block);
		registerForUndo(block);
		MaterialAndData applyMaterial = new MaterialAndData(material);
		if (block.getType() == Material.SNOW && material == Material.SNOW)
		{
			if (block.getData() < 7)
			{
				applyMaterial.setData((byte)(block.getData() + 1));
			}
		}
		applyMaterial.modify(block);
		return SpellResult.CAST;
	}

	@Override
	public void getParameterNames(Collection<String> parameters)
	{
		super.getParameterNames(parameters);
		parameters.add("time");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		if (parameterKey.equals("ice")) {
			examples.add("ice");
			examples.add("packed_ice");
		} else if (parameterKey.equals("freeze_water") || parameterKey.equals("freeze_lava") || parameterKey.equals("freeze_fire")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
		} else {
			super.getParameterOptions(examples, parameterKey);
		}
	}
}
