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

import com.elmakers.mine.bukkit.api.automata.Automaton;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.block.BlockRule;

public class AutomatonRule extends BlockRule {
    private MaterialSet replace;
    private Deque<WeightedPair<String>> automatonProbability = new ArrayDeque<>();
    private ConfigurationSection parameters;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(automatonProbability, parameters, "automaton");
        this.parameters = parameters.getConfigurationSection("automaton_parameters");
        replace = controller.getMaterialSetManager().fromConfig(parameters.getString("replace"));
        logBlockRule("Creating automata " + StringUtils.join(RandomUtils.getValues(automatonProbability), ","));
        return !automatonProbability.isEmpty();
    }

    @Override
    @Nonnull
    public BlockResult onHandle(Block block, Random random, Player cause) {
        if (replace != null && !replace.testBlock(block)) {
            return BlockResult.SKIP;
        }
        String automatonKey = RandomUtils.weightedRandom(automatonProbability);

        if (automatonKey.equalsIgnoreCase("none")) {
            return BlockResult.SKIP;
        }
        try {
            BlockResult result = BlockResult.valueOf(automatonKey.toUpperCase());
            return result;
        } catch (Exception ignore) {
        }
        Mage mage = controller.getMage(cause);
        Location location = block.getLocation();
        Automaton automaton = controller.addAutomaton(location, automatonKey, mage.getId(), mage.getName(), parameters);
        String message = " automaton: " + automatonKey + " at " + location.getWorld().getName() + "," + location.toVector();
        if (automaton == null) {
            message = "Failed to create" + message;
        } else {
            message = "Created" + message;
        }
        controller.info(message);
        return automaton == null ? BlockResult.SKIP : BlockResult.REMOVE_DROPS;
    }
}
