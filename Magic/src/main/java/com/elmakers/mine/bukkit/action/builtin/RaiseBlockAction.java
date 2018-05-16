package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockFace;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DirectionUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class RaiseBlockAction extends BaseSpellAction
{
    private int verticalSearchDistance;
    private List<BlockFace> directions;
    private Deque<WeightedPair<Integer>> slopeProbability;
    private boolean consumeBlocks = false;
    private boolean consumeVariants = true;
    private int maxHeight;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        if (parameters.contains("slopes")) {
            slopeProbability = new ArrayDeque<>();
            RandomUtils.populateIntegerProbabilityMap(slopeProbability, ConfigurationUtils.getConfigurationSection(parameters, "slopes"));
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        consumeBlocks = parameters.getBoolean("consume", false);
        consumeVariants = parameters.getBoolean("consume_variants", true);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
        directions = DirectionUtils.getDirections(parameters, "faces");
        maxHeight = parameters.getInt("max_height", 0);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Block targetBlock = context.getTargetBlock();

        if (!context.hasBuildPermission(targetBlock)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (consumeBlocks && !context.isConsumeFree()) {
            UndoList undoList = context.getUndoList();
            if (undoList != null) {
                undoList.setConsumed(true);
            }
        }

        List<Material> underground = new ArrayList<>();
        List<Material> ground = new ArrayList<>();
        int maxHeight = targetBlock.getY();
        for (BlockFace face : directions) {
            Block check = face.getRelative(targetBlock);
            if (context.isTransparent(check)) continue;

            int vertical = 0;
            while (vertical <= verticalSearchDistance) {
                vertical++;
                Block next = BlockFace.UP.getRelative(check);
                if (context.isTransparent(next)) {
                    ground.add(check.getType());
                    break;
                } else {
                    underground.add(check.getType());
                    check = next;
                    maxHeight = Math.max(check.getY(), maxHeight);
                }
            }
        }

        if (this.maxHeight > 0 && maxHeight > this.maxHeight) {
            maxHeight = this.maxHeight;
        }

        int slope = slopeProbability == null ? 0 : RandomUtils.weightedRandom(slopeProbability);
        if (maxHeight - targetBlock.getY() <= slope) {
            return SpellResult.NO_TARGET;
        }

        Mage mage = context.getMage();
        int targetHeight = maxHeight - slope;
        while (targetBlock.getY() < targetHeight) {
            targetBlock = BlockFace.UP.getRelative(targetBlock);
            if (!context.hasBuildPermission(targetBlock)) {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
            if (!context.isTransparent(targetBlock)) continue;
            if (!context.isDestructible(targetBlock)) continue;

            context.registerForUndo(targetBlock);
            List<Material> materials = targetBlock.getY() == targetHeight ? ground : underground;
            Material material = RandomUtils.getRandom(materials);

            if (consumeBlocks && !context.isConsumeFree()) {
                ItemStack requires = new ItemStack(material);
                if (!mage.hasItem(requires, consumeVariants)) {
                    String requiresMessage = context.getMessage("insufficient_resources");
                    context.sendMessage(requiresMessage.replace("$cost", MaterialAndData.getMaterialName(requires)));
                    return SpellResult.STOP;
                }
                mage.removeItem(requires, consumeVariants);
            }

            targetBlock.setType(material);
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
    public boolean requiresBuildPermission() {
        return true;
    }
}
