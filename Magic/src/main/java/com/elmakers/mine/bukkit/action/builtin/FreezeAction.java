package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class FreezeAction extends BaseSpellAction
{
    private boolean freezeWater;
    private boolean freezeLava;
    private boolean freezeFire;
    private Material iceMaterial;


    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        freezeWater = parameters.getBoolean("freeze_water", true);
        freezeLava = parameters.getBoolean("freeze_lava", true);
        freezeFire = parameters.getBoolean("freeze_fire", true);
        iceMaterial = ConfigurationUtils.getMaterial(parameters, "ice", Material.ICE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context)
    {
        Block block = context.getTargetBlock();
        Material material = Material.SNOW;
        if (block.getType() == Material.WATER)
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
        else if (context.isTransparent(block.getType()))
        {
            return SpellResult.NO_TARGET;
        }
        else
        {
            block = block.getRelative(BlockFace.UP);

            // This is kind of ugly, maybe clean it up somehow?
            if (block.getType() == Material.WATER)
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
            else if (block.getType() == Material.FIRE)
            {
                if (!freezeFire)
                {
                    return SpellResult.NO_TARGET;
                }
                material = Material.AIR;
            }
        }
        if (!context.isDestructible(block))
        {
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(block);
        MaterialAndData applyMaterial = new MaterialAndData(material);
        if (block.getType() == Material.SNOW && material == Material.SNOW)
        {
            short data = block.getData();
            if (data < 7)
            {
                data++;
            }
            applyMaterial.setData(data);
        }
        applyMaterial.modify(block);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("freeze_water");
        parameters.add("ice");
        parameters.add("freeze_lava");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("ice")) {
            examples.add("ice");
            examples.add("packed_ice");
        } else if (parameterKey.equals("freeze_water") || parameterKey.equals("freeze_lava") || parameterKey.equals("freeze_fire")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
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
