package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.automata.Automaton;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CreateAutomatonAction extends BaseSpellAction {
    private String automatonKey;
    private ConfigurationSection parameters;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        automatonKey = parameters.getString("automaton");
        parameters = parameters.getConfigurationSection("automaton_parameters");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Location location = context.getTargetLocation();
        Automaton automaton = controller.addAutomaton(location, automatonKey, mage.getId(), mage.getName(), parameters);
        return automaton == null ? SpellResult.FAIL : SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
