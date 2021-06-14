package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class DeprecatedUtilsBase implements DeprecatedUtils {
    protected final Platform platform;

    protected DeprecatedUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
