package com.elmakers.mine.bukkit.api.item;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public interface Icon {
    @Nullable
    String getUrl();

    @Nullable
    String getUrlDisabled();

    @Nullable
    String getGlyph();

    /**
     * Automatically get the URL, legacy or modern version of this icon, depending on icon and server settings
     */
    @Nullable
    MaterialAndData getItemMaterial(MageController controller);

    /**
     * Don't use this one, use the MageController version instead.
     */
    @Nullable
    @Deprecated
    MaterialAndData getItemMaterial(boolean isLegacy);

    /**
     * Automatically get the URL, legacy or modern version of this icon, depending on icon and server settings
     */
    @Nullable
    MaterialAndData getItemDisabledMaterial(MageController controller);

    /**
     * Don't use this one, use the MageController version instead.
     */
    @Nullable
    @Deprecated
    MaterialAndData getItemDisabledMaterial(boolean isLegacy);

    boolean forceUrl();

    @Nullable
    String getType();
}
