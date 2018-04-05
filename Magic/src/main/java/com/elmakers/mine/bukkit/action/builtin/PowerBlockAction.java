package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

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
        if (block == null || !context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }
        if (!context.hasBuildPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        context.getUndoList().setApplyPhysics(true);

        Material material = block.getType();
        BlockState blockState = block.getState();
        BlockData data = block.getBlockData();
        MageController controller = context.getController();
        boolean powerBlock = false;
        if (data instanceof Powerable) {
            Powerable powerData = (Powerable)data;
            context.registerForUndo(block);
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof AnaloguePowerable) {
            AnaloguePowerable wireData = (AnaloguePowerable)data;
            context.registerForUndo(block);
            wireData.setPower((byte)(wireData.getMaximumPower() - wireData.getPower()));
            powerBlock = true;
        } else if (material == Material.REDSTONE_BLOCK) {
            context.registerForUndo(block);
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material.getId());
            controller.getRedstoneReplacement().modify(block, applyPhysics);
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