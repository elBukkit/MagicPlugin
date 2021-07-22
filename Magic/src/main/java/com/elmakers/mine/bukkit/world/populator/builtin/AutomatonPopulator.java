package com.elmakers.mine.bukkit.world.populator.builtin;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.RandomUtils;

@Deprecated
public class AutomatonPopulator extends MagicBlockPopulator {
    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        RandomUtils.populateStringProbabilityMap(templateProbability, parameters, "automaton");
        this.parameters = parameters.getConfigurationSection("automaton_parameters");
        replace = controller.getMaterialSetManager().fromConfig(parameters.getString("replace"));
        String message = "Creating automata " + StringUtils.join(RandomUtils.getValues(templateProbability), ",");
        if (replace != null) {
            message += " on generation of " + StringUtils.join(replace.getMaterials(), ",");
        }
        logBlockRule(message);
        controller.getLogger().warning("The Automaton populator is deprecated, please use MagicBlock instead");
        return !templateProbability.isEmpty();
    }
}
