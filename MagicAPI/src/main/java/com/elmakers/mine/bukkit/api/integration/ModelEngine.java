package com.elmakers.mine.bukkit.api.integration;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public interface ModelEngine {
    boolean applyModel(Entity entity, ConfigurationSection configuration);
    boolean removeModelState(Entity entity, @Nullable String model, String state, boolean ignoreLerp);
    boolean addModelState(Entity entity, @Nullable String model, String state, int lerpIn, int lerpOut, double speed);
}
