package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class PowerBlockAction extends BaseSpellAction {
    private boolean applyPhysics = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        applyPhysics = parameters.getBoolean("physics", false);
    }

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

        CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
        if (compatibilityUtils.canToggleBlockPower(block)) {
            context.registerForUndo(block);
            compatibilityUtils.toggleBlockPower(block);
        } else {
            Material material = block.getType();
            BlockState blockState = block.getState();
            MageController controller = context.getController();
            boolean updateBlockState = false;
            if (material == Material.REDSTONE_BLOCK) {
                context.registerForUndo(block);
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material);
                controller.getRedstoneReplacement().modify(block, applyPhysics);
            } else if (material == Material.TNT) {
                context.registerForUndo(block);
                block.setType(Material.AIR);

                // Kaboomy time!
                context.registerForUndo(block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT));
            }
            if (updateBlockState) {
                blockState.update();
            }
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
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
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
