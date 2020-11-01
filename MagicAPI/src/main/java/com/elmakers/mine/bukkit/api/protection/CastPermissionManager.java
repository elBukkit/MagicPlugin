package com.elmakers.mine.bukkit.api.protection;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

/**
 * Register via PreLoadEvent.register()
 */
public interface CastPermissionManager extends MagicProvider {
    /**
     * This will perform cast permission checks for a specific location.
     *
     * @return false to deny cast permission, null to not care. Returning true means the cast will be allowed,
     *     including breaking/building blocks, even if it otherwise would not be allowed.
     */
    @Nullable
    default Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location) {
        return null;
    }

    /**
     * This will perform cast permission checks for a specific location.
     * This will override the region permission, and is generally for use inside of a player's
     * personal protected area, when it may be contained within a larger globally protected area.
     *
     * @return false to deny cast permission, null to not care. Returning true means the cast will be allowed,
     *     including breaking/building blocks, even if it otherwise would not be allowed.
     */
    @Nullable
    default Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location) {
        return null;
    }
}
