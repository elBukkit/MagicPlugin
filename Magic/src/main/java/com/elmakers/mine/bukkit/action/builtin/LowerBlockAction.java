package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockFace;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DirectionUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class LowerBlockAction extends BaseSpellAction
{
    private int verticalSearchDistance;
    private List<BlockFace> directions;
    private Deque<WeightedPair<Integer>> slopeProbability;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        if (parameters.contains("slopes")) {
            slopeProbability = new ArrayDeque<>();
            RandomUtils.populateIntegerProbabilityMap(slopeProbability, ConfigurationUtils.getConfigurationSection(parameters, "slopes"));
        }
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
        directions = DirectionUtils.getDirections(parameters, "faces");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Block targetBlock = context.getTargetBlock();

        if (!context.hasBreakPermission(targetBlock)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        int minHeight = targetBlock.getY();
        for (BlockFace face : directions) {
            Block check = face.getRelative(targetBlock);

            int vertical = 0;
            while (vertical <= verticalSearchDistance && context.isTransparent(check)) {
                vertical++;
                check = BlockFace.DOWN.getRelative(check);
                minHeight = Math.min(check.getY(), minHeight);
            }
        }

        int slope = slopeProbability == null ? 0 : RandomUtils.weightedRandom(slopeProbability);
        if (targetBlock.getY() - minHeight <= slope) {
            return SpellResult.NO_TARGET;
        }

        int targetHeight = minHeight + slope;
        while (targetBlock.getY() > targetHeight) {
            if (!context.hasBreakPermission(targetBlock)) {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
            if (!context.isDestructible(targetBlock)) {
                return SpellResult.NO_TARGET;
            }

            context.registerForUndo(targetBlock);
            targetBlock.setType(Material.AIR);

            targetBlock = BlockFace.DOWN.getRelative(targetBlock);
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresBreakPermission() {
        return true;
    }
}
