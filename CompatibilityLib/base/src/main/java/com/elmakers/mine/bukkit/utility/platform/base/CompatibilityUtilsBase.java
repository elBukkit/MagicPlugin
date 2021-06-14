package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class CompatibilityUtilsBase implements CompatibilityUtils {
    protected final Platform platform;

    protected CompatibilityUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
