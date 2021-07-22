package com.elmakers.mine.bukkit.world.block.builtin;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.RandomUtils;

@Deprecated
public class AutomatonRule extends MagicBlockRule {

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(templateProbability, parameters, "automaton");
        this.parameters = parameters.getConfigurationSection("automaton_parameters");
        replace = controller.getMaterialSetManager().fromConfig(parameters.getString("replace"));
        logBlockRule("Creating automata " + StringUtils.join(RandomUtils.getValues(templateProbability), ","));
        controller.getLogger().warning("The Automaton rule is deprecated, please use MagicBlock instead");
        return !templateProbability.isEmpty();
    }
}
