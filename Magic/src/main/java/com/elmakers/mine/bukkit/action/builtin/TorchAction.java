package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class TorchAction extends BaseSpellAction
{
    private boolean allowLightstone;
    private boolean allowSeaLantern;
    private boolean useLightstone;
    private boolean useRedstone;
    private MaterialSet slippery;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useRedstone = parameters.getBoolean("redstone_torch", false);
        allowLightstone = parameters.getBoolean("allow_glowstone", false);
        allowSeaLantern = parameters.getBoolean("allow_sea_lantern", false);
        useLightstone = parameters.getBoolean("glowstone_torch", false);
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        MaterialSetManager materialSetManager = spell.getController().getMaterialSetManager();
        slippery = materialSetManager.fromConfig(parameters.getString("not_attachable", "not_attachable"));
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

        if (slippery != null && slippery.testBlock(target))
        {
            return SpellResult.NO_TARGET;
        }

        boolean isAir = DefaultMaterials.isAir(face.getType());
        boolean replaceAttachment = target.getType() == Material.SNOW || target.getType() == Material.SNOW_BLOCK;
        boolean isWater = DefaultMaterials.isWater(face.getType());
        boolean isNether = target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
        MaterialAndData targetMaterial = null;

        // Don't replace blocks unless allow_glowstone is explicitly set
        if (isNether && allowLightstone)
        {
            targetMaterial = new MaterialAndData(Material.GLOWSTONE);
            replaceAttachment = true;
        }

        // Otherwise use glowstone as the torch
        boolean allowLightstone = this.allowLightstone;
        if (useLightstone)
        {
            targetMaterial = new MaterialAndData(Material.GLOWSTONE);
            allowLightstone = true;
        }
        if (isWater)
        {
            if (allowSeaLantern) {
                targetMaterial = new MaterialAndData(Material.SEA_LANTERN);
            } else {
                targetMaterial = new MaterialAndData(Material.GLOWSTONE);
            }
        }

        if (!isAir && !isWater)
        {
            return SpellResult.NO_TARGET;
        }

        BlockFace direction = BlockFace.SELF;
        if (targetMaterial == null)
        {
            direction = face.getFace(target);
            if (direction == null) {
                direction = BlockFace.SELF;
            }
            switch (direction)
            {
                case WEST:
                    targetMaterial = useRedstone
                        ? new MaterialAndData(DefaultMaterials.getRedstoneWallTorchOn())
                        : new MaterialAndData(DefaultMaterials.getWallTorch());
                    targetMaterial.setData((short)(targetMaterial.getData() | 1));
                    break;
                case EAST:
                    targetMaterial = useRedstone
                        ? new MaterialAndData(DefaultMaterials.getRedstoneWallTorchOn())
                        : new MaterialAndData(DefaultMaterials.getWallTorch());
                    targetMaterial.setData((short)(targetMaterial.getData() | 2));
                    break;
                case NORTH:
                    targetMaterial = useRedstone
                        ? new MaterialAndData(DefaultMaterials.getRedstoneWallTorchOn())
                        : new MaterialAndData(DefaultMaterials.getWallTorch());
                    targetMaterial.setData((short)(targetMaterial.getData() | 3));
                    break;
                case SOUTH:
                    targetMaterial = useRedstone
                        ? new MaterialAndData(DefaultMaterials.getRedstoneWallTorchOn())
                        : new MaterialAndData(DefaultMaterials.getWallTorch());
                    targetMaterial.setData((short)(targetMaterial.getData() | 4));
                    break;
                default:
                    targetMaterial = useRedstone
                        ? new MaterialAndData(DefaultMaterials.getRedstoneTorchOn())
                        : new MaterialAndData(Material.TORCH);
                    targetMaterial.setData((short)(targetMaterial.getData() | 5));
                    break;
            }
        }

        if (!allowSeaLantern && targetMaterial.getMaterial() == Material.SEA_LANTERN)
        {
            return SpellResult.NO_TARGET;
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
        context.getController().disableItemSpawn();
        try {
            targetMaterial.modify(target);
            if (direction != BlockFace.SELF) {
                CompatibilityLib.getCompatibilityUtils().setTorchFacingDirection(target, direction.getOppositeFace());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        context.getController().enableItemSpawn();
        if (targetMaterial.getMaterial() != target.getType())
        {
            return SpellResult.NO_TARGET;
        }

        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("redstone_torch");
        parameters.add("allow_glowstone");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("redstone_torch") || parameterKey.equals("allow_glowstone")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
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
