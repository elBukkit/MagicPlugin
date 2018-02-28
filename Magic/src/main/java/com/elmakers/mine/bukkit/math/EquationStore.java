package com.elmakers.mine.bukkit.math;

import de.slikey.effectlib.math.EquationTransform;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EquationStore {
    private static EquationStore instance;
    private Map<String, EquationTransform> transforms = new HashMap<>();

    public static final String DEFAULT_VARIABLE = "x";
    private static final String[] _SINGLE_ARRAY = {DEFAULT_VARIABLE};
    public static final Set<String> DEFAULT_SINGLE_VARIABLES = new HashSet<>(Arrays.asList(_SINGLE_ARRAY));

    public EquationTransform getTransform(String equation) {
        return getTransform(equation, DEFAULT_SINGLE_VARIABLES);
    }
    
    public EquationTransform getTransform(String equation, Set<String> variables) {
        EquationTransform transform = transforms.get(equation);
        if (transform == null) {
            transform = new EquationTransform(equation, variables, true);
            transforms.put(equation, transform);
        }
        
        return transform;
    }
    
    public static EquationStore getInstance() {
        if (instance == null) {
            instance = new EquationStore();
        }
        
        return instance;
    }
}
