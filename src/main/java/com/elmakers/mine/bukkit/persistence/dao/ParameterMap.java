package com.elmakers.mine.bukkit.persistence.dao;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

public class ParameterMap extends HashMap<String, ParameterData>
{
    private static final long serialVersionUID = 1L;

    public void addAll(List<ParameterData> parameters)
    {
        for (ParameterData parameter : parameters)
        {
            put(parameter.getId(), parameter);
        }
    }
    
    public boolean hasFlag(String flagName)
    {
        ParameterData flag = get(flagName);
        return flag != null && flag.isFlag();
    }
    
    public boolean hasParameter(String paramName)
    {
        ParameterData flag = get(paramName);
        return flag != null;
    }
    
    public int getInteger(String name, int defaultValue)
    {
        ParameterData parameter = get(name);
        if (parameter == null || parameter.getType() != ParameterType.INTEGER)
        {
            return defaultValue;
        }
        
        return parameter.getInteger();
    }
    
    public double getDouble(String name, double defaultValue)
    {
        ParameterData parameter = get(name);
        if (parameter == null || parameter.getType() != ParameterType.DOUBLE)
        {
            return defaultValue;
        }
        
        return parameter.getDouble();
    }
    
    public boolean getBoolean(String name, boolean defaultValue)
    {
        ParameterData parameter = get(name);
        if (parameter == null || parameter.getType() != ParameterType.BOOLEAN)
        {
            return defaultValue;
        }
        
        return parameter.getBoolean();
    }
    
    public Material getMaterial(String name, Material defaultValue)
    {
        ParameterData parameter = get(name);
        if (parameter == null || parameter.getType() != ParameterType.MATERIAL)
        {
            return defaultValue;
        }
        
        return parameter.getMaterial();
    }
    
    public String getString(String name, String defaultValue)
    {
        ParameterData parameter = get(name);
        if (parameter == null || parameter.getType() != ParameterType.STRING)
        {
            return defaultValue;
        }
        
        return parameter.getValue();
    }

}
