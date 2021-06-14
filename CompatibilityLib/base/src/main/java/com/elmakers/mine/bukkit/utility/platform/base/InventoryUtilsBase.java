package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class InventoryUtilsBase implements InventoryUtils {
    protected final Platform platform;

    protected InventoryUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
