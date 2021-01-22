package com.elmakers.mine.bukkit.api.action;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;

public interface ActionHandler {
    SpellResult perform(CastContext context);
    void prepare(CastContext context, ConfigurationSection parameters);
    // Whoops- why is this class in the API again?
    // You definitely shouldn't be implementing one :)
    default void processParameters(CastContext context, ConfigurationSection parameters) {};
    void finish(CastContext context);
    int size();
}
