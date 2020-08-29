package com.elmakers.mine.bukkit.api.protection;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

public interface PlayerWarpManager {
    @Nullable
    Collection<PlayerWarp> getWarps(@Nonnull Player player);
}
