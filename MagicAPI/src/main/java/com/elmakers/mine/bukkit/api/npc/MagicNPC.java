package com.elmakers.mine.bukkit.api.npc;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.api.entity.EntityData;

public interface MagicNPC {
    @Nonnull
    EntityData getEntityData();
    @Nullable
    Location getLocation();
    @Nonnull
    String getName();
    void setName(@Nonnull String name);
    void configure(String key, Object value);
    @Nonnull
    UUID getUUID();
    void teleport(@Nonnull Location location);
    boolean setType(@Nonnull String mobKey);
    void remove();
}
