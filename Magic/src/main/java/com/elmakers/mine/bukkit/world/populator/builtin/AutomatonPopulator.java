package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.automata.Automaton;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.populator.MagicBlockPopulator;

public class AutomatonPopulator extends MagicBlockPopulator {
    private MaterialSet replace;
    private Deque<WeightedPair<String>> automatonProbability = new ArrayDeque<>();
    private ConfigurationSection parameters;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(automatonProbability, parameters, "automaton");
        this.parameters = parameters.getConfigurationSection("automaton_parameters");
        replace = controller.getMaterialSetManager().fromConfig(parameters.getString("replace"));
        String message = "Creating automata " + StringUtils.join(RandomUtils.getValues(automatonProbability), ",");
        if (replace != null) {
            message += " on generation of " + StringUtils.join(replace.getMaterials(), ",");
        }
        logBlockRule(message);
        return !automatonProbability.isEmpty();
    }

    @Override
    public BlockResult populate(Block block, Random random) {
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
        Location location = block.getLocation();
        Automaton automaton = controller.addAutomaton(location, automatonKey, null, null, parameters);
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
