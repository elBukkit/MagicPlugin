package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class BurnAction extends BaseSpellAction
{
    private Material fireMaterial;
    private MaterialSet allFire;
    private boolean meltIce;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        meltIce = parameters.getBoolean("melt_ice", true);
        allFire = context.getController().getMaterialSetManager().fromConfig(parameters.getString("all_fire", "all_fire"));
        String fireString = parameters.getString("fire_type", "fire");
        try {
            fireMaterial = Material.valueOf(fireString.toUpperCase());
        } catch (Exception ex) {
            context.getLogger().warning("Invalid material for fire_type: " + fireString);
            fireMaterial = Material.FIRE;
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Block block = context.getTargetBlock();
        if (block == null || DefaultMaterials.isAir(block.getType()) || allFire.testBlock(block) || DefaultMaterials.isWater(block.getType()))
        {
            return SpellResult.NO_TARGET;
        }
        Material material = fireMaterial;
        if (block.getType() == Material.ICE || block.getType() == Material.SNOW || block.getType() == Material.PACKED_ICE || block.getType() == Material.FROSTED_ICE)
        {
            if (!meltIce) {
                return SpellResult.NO_TARGET;
            }
            material = Material.AIR;
        }
        else
        {
            block = block.getRelative(BlockFace.UP);
        }
        if (allFire.testBlock(block) || DefaultMaterials.isWater(block.getType()))
        {
            return SpellResult.NO_TARGET;
        }
        if (!context.isDestructible(block) || !context.hasBuildPermission(block))
        {
            return SpellResult.NO_TARGET;
        }
        context.registerForUndo(block);
        MaterialAndData applyMaterial = new MaterialAndData(material);
        applyMaterial.modify(block);

        return SpellResult.CAST;
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
