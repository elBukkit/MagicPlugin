package com.elmakers.mine.bukkit.world.block.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.block.BlockRule;

public class DropRule extends BlockRule {
    protected Deque<WeightedPair<String>> dropProbability;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        dropProbability = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(dropProbability, parameters, "items");
        logBlockRule("Dropping one of " + StringUtils.join(dropProbability, ","));
        return !dropProbability.isEmpty();
    }

    @Override
    @Nonnull
    public BlockResult onHandle(Block block, Random random, Player player) {
        String itemKey = RandomUtils.weightedRandom(dropProbability);
        if (itemKey.isEmpty() || itemKey.equals("none")) {
            return BlockResult.REMOVE_DROPS;
        }

        try {
            // Look for a specific return type
            return BlockResult.valueOf(itemKey.toUpperCase());
        } catch (Exception ignore) {
        }

        ItemData itemData = controller.getItem(itemKey);
        ItemStack itemStack = itemData == null ? null : itemData.getItemStack();
        if (itemStack == null) {
            controller.getLogger().warning("Invalid item key in drop rule: " + itemKey);
            return BlockResult.SKIP;
        }
        player.getWorld().dropItemNaturally(block.getLocation(), itemStack);
        return BlockResult.REPLACED_DROPS;
    }
}
