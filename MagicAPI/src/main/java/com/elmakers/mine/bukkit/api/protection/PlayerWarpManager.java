package com.elmakers.mine.bukkit.api.protection;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

/**
 * Register via PreLoadEvent.registerWarpManager()
 * This has been replaced by PlayerWarpProvider.
 */
public interface PlayerWarpManager {
    /**
     * Return warps for a given players.
     *
     * @param player The player who is getting warps
     * @return A list of warps
     */
    @Nullable
    Collection<PlayerWarp> getWarps(@Nonnull Player player);

    /**
     * Return all warps.
     *
     * @return A list of warps
     */
    @Nullable
    default Collection<PlayerWarp> getAllWarps() {
        return null;
    }
}
