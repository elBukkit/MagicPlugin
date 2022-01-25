package com.elmakers.mine.bukkit.warp;

import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.warp.WarpDescription;

public class MagicWarpDescription implements WarpDescription {
    private final String key;
    private final boolean maintainDirection;

    public MagicWarpDescription(String key, boolean maintainDirection) {
        this.key = key;
        this.maintainDirection = maintainDirection;
    }

    public MagicWarpDescription(String key) {
        this.key = key;
        this.maintainDirection = true;
    }

    @NotNull
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isMaintainDirection() {
        return maintainDirection;
    }
}
