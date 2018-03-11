package com.elmakers.mine.bukkit.magic;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Set;

public abstract class ParameterizedConfiguration extends ParameterizedConfigurationSection implements Configuration {
    private class Options extends ConfigurationOptions {
        protected Options(Configuration configuration) {
            super(configuration);
        }
    }
    
    private Options options;
    
    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    public void addDefaults(Map<String, Object> map) {

    }

    @Override
    public void addDefaults(Configuration configuration) {

    }

    @Override
    public void setDefaults(Configuration configuration) {

    }

    @Override
    public Configuration getDefaults() {
        return null;
    }

    @Override
    public ConfigurationOptions options() {
        if (options == null) {
            options = new Options(this);
        }

        return options;
    }

    protected Double evaluate(String expression) {
        Set<String> parameters = getParameters();
        if (parameters == null || parameters.isEmpty()) return null;

        EquationTransform transform = EquationStore.getInstance().getTransform(expression, parameters);
        for (String parameter : transform.getParameters()) {
            double value = getParameter(parameter);
            transform.setVariable(parameter, value);
        }

        return transform.get();
    }
    
    protected abstract Set<String> getParameters();
    protected abstract double getParameter(String parameter);
}
