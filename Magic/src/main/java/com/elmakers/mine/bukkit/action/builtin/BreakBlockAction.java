package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Collection;

public class BreakBlockAction extends BaseSpellAction {
    private int durabilityAmount;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        durabilityAmount = parameters.getInt("break_durability", 1);
    }

    @Override
    public SpellResult perform(CastContext context) {
        MaterialBrush brush = context.getBrush();
        if (brush == null) {
            return SpellResult.FAIL;
        }
        Block block = context.getTargetBlock();
        if (block.getType() == Material.AIR || !context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }
        context.registerForUndo(block);
        double breakAmount = 1;
        double durability = CompatibilityUtils.getDurability(block.getType());
        if (durability > 0) {
            double breakPercentage = durabilityAmount / durability;
            breakAmount = context.registerBreaking(block, breakPercentage);
        }

        if (breakAmount > 1) {
            if (context.hasBreakPermission(block)) {
                CompatibilityUtils.setBreaking(block, 10, UndoList.BLOCK_BREAK_RANGE);
                BlockState blockState = block.getState();
                if (blockState != null && (blockState instanceof InventoryHolder || blockState.getType() == Material.FLOWER_POT)) {
                    NMSUtils.clearItems(blockState.getLocation());
                }
                block.setType(Material.AIR);
                context.unregisterBreaking(block);
                context.playEffects("break");
            }
        } else {
            int breakState = (int)Math.floor(9 * breakAmount);
            CompatibilityUtils.setBreaking(block, breakState, UndoList.BLOCK_BREAK_RANGE);
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("durability");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("durability")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
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