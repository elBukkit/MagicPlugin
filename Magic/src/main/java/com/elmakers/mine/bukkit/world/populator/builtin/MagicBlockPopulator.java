package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class MagicBlockPopulator extends BaseBlockPopulator {
    protected MaterialSet replace;
    protected Deque<WeightedPair<String>> templateProbability = new ArrayDeque<>();
    protected ConfigurationSection parameters;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(templateProbability, parameters, "template");
        this.parameters = parameters.getConfigurationSection("block_parameters");
        replace = controller.getMaterialSetManager().fromConfig(parameters.getString("replace"));
        String message = "Creating magic block " + StringUtils.join(RandomUtils.getValues(templateProbability), ",");
        if (replace != null) {
            message += " on generation of " + StringUtils.join(replace.getMaterials(), ",");
        }
        logBlockRule(message);
        return !templateProbability.isEmpty();
    }

    @Override
    public BlockResult populate(Block block, Random random) {
        if (replace != null && !replace.testBlock(block)) {
            return BlockResult.SKIP;
        }
        String templateKey = RandomUtils.weightedRandom(templateProbability);

        if (templateKey.equalsIgnoreCase("none")) {
            return BlockResult.SKIP;
        }
        try {
            BlockResult result = BlockResult.valueOf(templateKey.toUpperCase());
            return result;
        } catch (Exception ignore) {
        }
        Location location = block.getLocation();
        MagicBlock automaton = controller.addMagicBlock(location, templateKey, null, null, parameters);
        String message = " magic block: " + templateKey + " at " + location.getWorld().getName() + "," + location.toVector();
        if (automaton == null) {
            message = "Failed to create" + message;
        } else {
            message = "Created" + message;
        }
        controller.info(message);
        return automaton == null ? BlockResult.SKIP : BlockResult.REMOVE_DROPS;
    }
}
