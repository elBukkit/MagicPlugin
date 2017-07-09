package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PoweredRail;
import org.bukkit.material.RedstoneWire;

import java.util.Arrays;
import java.util.Collection;

public class PowerBlockAction extends BaseSpellAction {
    private boolean applyPhysics = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        applyPhysics = parameters.getBoolean("physics", false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
        if (!context.hasBuildPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }
        context.getUndoList().setApplyPhysics(true);

        Material material = block.getType();
        BlockState blockState = block.getState();
        MaterialData data = blockState.getData();
        MageController controller = context.getController();
        boolean powerBlock = false;
        if (data instanceof Button) {
            Button powerData = (Button)data;
            context.registerForUndo(block);
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof Lever) {
            Lever powerData = (Lever)data;
            context.registerForUndo(block);
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof PistonBaseMaterial) {
            PistonBaseMaterial powerData = (PistonBaseMaterial)data;
            context.registerForUndo(block);
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof PoweredRail) {
            PoweredRail powerData = (PoweredRail)data;
            context.registerForUndo(block);
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof RedstoneWire) {
            RedstoneWire wireData = (RedstoneWire)data;
            context.registerForUndo(block);
            wireData.setData((byte)(15 - wireData.getData()));
            powerBlock = true;
        } else if (material == Material.REDSTONE_BLOCK) {
            context.registerForUndo(block);
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material.getId());
            controller.getRedstoneReplacement().modify(block, applyPhysics);
        } else if (material == Material.REDSTONE_TORCH_OFF) {
            context.registerForUndo(block);
            block.setType(Material.REDSTONE_TORCH_ON);
        } else if (material == Material.REDSTONE_TORCH_ON) {
            context.registerForUndo(block);
            block.setType(Material.REDSTONE_TORCH_OFF);
        } else if (material == Material.TNT) {
            context.registerForUndo(block);
            block.setType(Material.AIR);

            // Kaboomy time!
            context.registerForUndo(block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT));
        }

        if (powerBlock) {
            blockState.update();
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("physics");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("physics")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}