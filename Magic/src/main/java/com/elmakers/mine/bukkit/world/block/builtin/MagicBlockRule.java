package com.elmakers.mine.bukkit.world.block.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.block.BlockRule;

public class MagicBlockRule extends BlockRule {
    protected MaterialSet replace;
    protected Deque<WeightedPair<String>> templateProbability = new ArrayDeque<>();
    protected ConfigurationSection parameters;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(templateProbability, parameters, "template");
        this.parameters = parameters.getConfigurationSection("block_parameters");
        replace = controller.getMaterialSetManager().fromConfig(parameters.getString("replace"));
        logBlockRule("Creating magic block " + StringUtils.join(RandomUtils.getValues(templateProbability), ","));
        return !templateProbability.isEmpty();
    }

    @Override
    @Nonnull
    public BlockResult onHandle(Block block, Random random, Player cause) {
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
        Mage mage = controller.getMage(cause);
        Location location = block.getLocation();
        MagicBlock magicBlock = controller.addMagicBlock(location, templateKey, mage.getId(), mage.getName(), parameters);
        String message = " magic block: " + templateKey + " at " + location.getWorld().getName() + "," + location.toVector();
        if (magicBlock == null) {
            message = "Failed to create" + message;
        } else {
            message = "Created" + message;
        }
        controller.info(message);
        return magicBlock == null ? BlockResult.SKIP : BlockResult.REMOVE_DROPS;
    }
}
