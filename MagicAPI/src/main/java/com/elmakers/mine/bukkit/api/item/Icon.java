package com.elmakers.mine.bukkit.api.item;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

public interface Icon {
    @Nullable
    String getUrl();

    @Nullable
    String getUrlDisabled();

    @Nullable
    String getGlyph();

    @Nullable
    MaterialAndData getItemMaterial(boolean isLegacy);

    @Nullable
    MaterialAndData getItemDisabledMaterial(boolean isLegacy);

    boolean forceUrl();
}
