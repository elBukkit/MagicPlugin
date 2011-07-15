package com.elmakers.mine.bukkit.utilities;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.util.config.ConfigurationNode;

public class PluginConfigurationNode extends ConfigurationNode
{
    protected PluginConfigurationNode(Map<String, Object> root)
    {
        super(root);
    }
    
    @SuppressWarnings("unchecked")
    public PluginConfigurationNode createChild(String name)
    {
        Map<String, Object> newChild = new HashMap<String, Object>();
        
        setProperty(name, newChild);
        
        Object raw = getProperty(name);

        if (raw instanceof Map) 
        {
            return new PluginConfigurationNode((Map<String, Object>) raw);
        }

        return null;
    }

}
