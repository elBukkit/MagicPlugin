package com.elmakers.mine.bukkit.api.warp;

import javax.annotation.Nonnull;

public interface WarpDescription {
    @Nonnull
    String getKey();
    boolean isMaintainDirection();
}
