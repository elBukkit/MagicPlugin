package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;

@Deprecated
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
            if (parameters.getBoolean("cycle_moon_phase", false))
            {

                long currentTime = world.getFullTime();
                currentTime = ((currentTime % 24000) + 1) * 24000 + targetTime;
                world.setFullTime(currentTime);
                return SpellResult.ALTERNATE;
            }

            world.setTime(targetTime);
            return SpellResult.ALTERNATE;
        }

        boolean allowNight = parameters.getBoolean("allow_night", false);
        boolean allowDay = parameters.getBoolean("allow_day", false);
        if (isLookingUp() && allowDay)
        {
            timeType = "day";
            world.setTime(0);
            return SpellResult.ALTERNATE;
        }


        if (isLookingDown() && allowNight)
        {
            timeType = "night";
            world.setTime(13000);
            return SpellResult.ALTERNATE;
        }

        Block target = getTargetBlock();
        Block face = getPreviousBlock();

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
        boolean isWater = DefaultMaterials.isWater(face.getType());
        boolean isNether = target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
        Material targetMaterial = Material.TORCH;

        if (isWater || isAttachmentSlippery || isNether)
        {
            targetMaterial = Material.GLOWSTONE;
            replaceAttachment = true;
        }

        boolean allowLightstone = parameters.getBoolean("allow_glowstone", false);
        if ((!isAir && !isWater)
                || (targetMaterial == Material.GLOWSTONE && !allowLightstone))
        {
            return SpellResult.NO_TARGET;
        }

        BlockFace direction = face.getFace(target);
        if (!replaceAttachment)
        {
            target = face;
        }

        byte data = 0;
        if (targetMaterial == Material.TORCH)
        {
            switch (direction)
            {
                case WEST:
                    data = 1;
                    break;
                case EAST:
                    data = 2;
                    break;
                case NORTH:
                    data = 3;
                    break;
                case SOUTH:
                    data = 4;
                    break;
                case DOWN:
                    data = 5;
                    break;
                default:
                    targetMaterial = Material.GLOWSTONE;
            }
        }

        if (!allowLightstone && targetMaterial == Material.GLOWSTONE) {
            return SpellResult.NO_TARGET;
        }

        registerForUndo(target);
        DeprecatedUtils.setTypeAndData(target, targetMaterial, data, false);
        registerForUndo();
        controller.updateBlock(target);

        return SpellResult.CAST;
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        return message.replace("$time", timeType);
    }
}
