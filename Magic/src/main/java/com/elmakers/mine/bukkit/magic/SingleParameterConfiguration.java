package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;

public class SingleParameterConfiguration extends ParameterizedConfiguration {
    private double value;
    
    public SingleParameterConfiguration(String storeKey, String variableName) {
        super(storeKey);
        checkParameters(variableName);
    }
    
    private void checkParameters(String variableName) {
        if (store.isEmpty()) {
            Set<String> newParameters = new HashSet<>();
            newParameters.add(variableName);
            store.initialize(newParameters);
        }
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    protected double getParameter(String parameter) {
        return value;
    }
}
