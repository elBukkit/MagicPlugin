package com.elmakers.mine.bukkit.integration;

import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface MythicMobManager {
    boolean initialize();
    boolean isEnabled();

    @Nullable
    Entity spawn(String key, Location location, double level);

    Collection<String> getMobKeys();

    boolean isMobKey(String mobKey);

    void setMobLevel(Entity entity, double level);

    @Nullable
    Double getMobLevel(Entity entity);

    @Nullable
    String getMobKey(Entity entity);
}
