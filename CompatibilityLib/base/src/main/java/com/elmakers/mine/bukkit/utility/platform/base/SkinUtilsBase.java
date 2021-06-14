package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public abstract class SkinUtilsBase implements SkinUtils {
    protected final Platform platform;

    protected SkinUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
