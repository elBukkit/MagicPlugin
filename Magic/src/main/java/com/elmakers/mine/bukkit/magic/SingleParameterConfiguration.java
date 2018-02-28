package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.math.EquationStore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SingleParameterConfiguration extends ParameterizedConfiguration {
    private double value;
    private Set<String> variables;

    public SingleParameterConfiguration(String variableName) {
        super();
        if (!variableName.equals(EquationStore.DEFAULT_VARIABLE)) {
            String[] variableArray = {variableName};
            variables = new HashSet<>(Arrays.asList(variableArray));
        } else {
            variables = EquationStore.DEFAULT_SINGLE_VARIABLES;
        }
    }
    
    public SingleParameterConfiguration() {
        super();
        variables = EquationStore.DEFAULT_SINGLE_VARIABLES;
    }
    
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    protected Set<String> getParameters() {
        return variables;
    }

    @Override
    protected double getParameter(String parameter) {
        return value;
    }
}
