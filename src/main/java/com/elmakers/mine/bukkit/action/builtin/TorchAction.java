package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class TorchAction extends BaseSpellAction
{
    private Material torchType;
    private boolean allowLightstone;
    private boolean useLightstone;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        torchType = parameters.getBoolean("redstone_torch", false) ? Material.REDSTONE_TORCH_ON : Material.TORCH;
        allowLightstone = parameters.getBoolean("allow_glowstone", false);
        useLightstone = parameters.getBoolean("glowstone_torch", false);
    }

	@Override
	public SpellResult perform(CastContext context) {
		Block face = context.getPreviousBlock();

		if (face == null)
		{
			return SpellResult.NO_TARGET;
		}
        Block target = context.getTargetBlock();
		if (!context.hasBuildPermission(target))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		boolean isAttachmentSlippery = target.getType() == Material.GLASS || target.getType() == Material.ICE;
		if (isAttachmentSlippery)
		{
			return SpellResult.NO_TARGET;
		}

		boolean isAir = face.getType() == Material.AIR;
		boolean replaceAttachment = target.getType() == Material.SNOW || target.getType() == Material.SNOW_BLOCK;
		boolean isWater = face.getType() == Material.STATIONARY_WATER || face.getType() == Material.WATER;
		boolean isNether = target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
		MaterialAndData targetMaterial = new MaterialAndData(torchType);

		// Don't replace blocks unless allow_glowstone is explicitly set
		if (isNether && allowLightstone)
		{
			targetMaterial.setMaterial(Material.GLOWSTONE);
			replaceAttachment = true;
		}

		// Otherwise use glowstone as the torch
        boolean allowLightstone = this.allowLightstone;
		if (useLightstone)
		{
			targetMaterial.setMaterial(Material.GLOWSTONE);
			allowLightstone = true;
		}
		if (isWater)
		{
			targetMaterial.setMaterial(Material.GLOWSTONE);
		}

		if (!isAir && !isWater)
		{
			return SpellResult.NO_TARGET;
		}

		if (targetMaterial.getMaterial() == torchType)
		{
			BlockFace direction = face.getFace(target);
			switch (direction)
			{
				case WEST:
					targetMaterial.setData((short)1);
					break;
				case EAST:
					targetMaterial.setData((short)2);
					break;
				case NORTH:
					targetMaterial.setData((short)3);
					break;
				case SOUTH:
					targetMaterial.setData((short)4);
					break;
				case DOWN:
					targetMaterial.setData((short)5);
					break;
				default:
					targetMaterial.setMaterial(Material.GLOWSTONE);
			}
		}

		if (!allowLightstone && targetMaterial.getMaterial() == Material.GLOWSTONE)
		{
			return SpellResult.NO_TARGET;
		}

		if (!replaceAttachment)
		{
			target = face;
		}

		context.registerForUndo(target);
		targetMaterial.modify(target);
        context.updateBlock(target);

		return SpellResult.CAST;
	}

	@Override
	public void getParameterNames(Collection<String> parameters) {
		super.getParameterNames(parameters);
		parameters.add("redstone_torch");
		parameters.add("allow_glowstone");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey) {
		if (parameterKey.equals("redstone_torch") || parameterKey.equals("allow_glowstone")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
		} else {
			super.getParameterOptions(examples, parameterKey);
		}
	}

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresBuildPermission()
    {
        return true;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }
}
