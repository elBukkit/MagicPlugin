package com.elmakers.mine.bukkit.magic;

import de.slikey.effectlib.math.EquationTransform;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ParameterizedConfiguration extends ParameterizedConfigurationSection implements Configuration {
    protected static class ParameterStore {
        protected Set<String> parameters;
        protected Map<String, EquationTransform> transforms;
        
        public void initialize(Set<String> newParameters) {
            parameters = null;
            transforms = null;
            if (newParameters == null || newParameters.isEmpty()) return;
            parameters = new HashSet<>();
            for (String parameter : newParameters) {
                parameters.add(parameter);
            }
            transforms = new HashMap<>();
        }
        
        public boolean isEmpty() {
            return parameters == null;
        }
        
        public EquationTransform getTransform(String expression) {
            EquationTransform transform = transforms.get(expression);
            if (transform == null) {
                transform = new EquationTransform(expression, parameters);
                transforms.put(expression, transform);
            }
            return transform;
        }
        
        public Set<String> getParameters() {
            return parameters;
        }
    }
    
    private class Options extends ConfigurationOptions {
        protected Options(Configuration configuration) {
            super(configuration);
        }
    }
    
    private static Map<String, ParameterStore> stores = new HashMap<>();
    protected final ParameterStore store;
    
    private Options options;
    
    public ParameterizedConfiguration(String storeKey) {
        super();
        store = getStore(storeKey);
    }
    
    protected static ParameterStore getStore(String storeKey) {
        ParameterStore targetStore = stores.get(storeKey);
        if (targetStore == null) {
            targetStore = new ParameterStore();
            stores.put(storeKey, targetStore);
        }
        return targetStore;
    }
    
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

    public static void initializeParameters(String storeKey, Set<String> parameters) {
        ParameterStore store = getStore(storeKey);
        store.initialize(parameters);
    }

    public static void checkParameters(String storeKey, Set<String> parameters) {
        ParameterStore store = getStore(storeKey);
        if (store.isEmpty()) {
            store.initialize(parameters);
        }
    }

    protected Double evaluate(String expression) {
        if (store.isEmpty()) return null;

        EquationTransform transform = store.getTransform(expression);
        for (String parameter : store.getParameters()) {
            double value = getParameter(parameter);
            transform.setVariable(parameter, value);
        }

        return transform.get();
    }
    
    protected abstract double getParameter(String parameter);
}
