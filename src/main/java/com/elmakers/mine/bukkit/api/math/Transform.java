package com.elmakers.mine.bukkit.api.math;

import org.bukkit.configuration.ConfigurationSection;

public interface Transform {
    public void load(ConfigurationSection parameters);
    public double get(double t);
}
