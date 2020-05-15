package com.elmakers.mine.bukkit.api.protection;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public interface CastPermissionManager {
    /**
     * This will perform cast permission checks for a specific location.
     */
    @Nullable
    default Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location) {
        return null;
    }

    /**
     * This will perform cast permission checks for a specific location.
     * This will override the region permission, and is generally for use inside of a player's
     * personal protected area, when it may be contained within a larger globally protected area.
     */
    @Nullable
    default Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location) {
        return null;
    }
}
