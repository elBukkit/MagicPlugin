package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class EntityUtilsBase implements EntityUtils {
    protected final Platform platform;

    protected EntityUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
