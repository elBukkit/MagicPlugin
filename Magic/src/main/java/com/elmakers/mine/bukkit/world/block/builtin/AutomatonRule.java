package com.elmakers.mine.bukkit.world.block.builtin;

import java.util.Deque;
import java.util.Random;
import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.automata.Automaton;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.block.BlockRule;

public class AutomatonRule extends BlockRule {
    private Deque<WeightedPair<String>> automatonProbability;
    private ConfigurationSection parameters;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(automatonProbability, parameters, "automaton");
        this.parameters = parameters.getConfigurationSection("automaton_parameters");
        return !automatonProbability.isEmpty();
    }

    @Override
    @Nonnull
    public BlockResult onHandle(Block block, Random random, Player cause) {
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
        return automaton == null ? BlockResult.SKIP : BlockResult.REMOVE_DROPS;
    }
}
