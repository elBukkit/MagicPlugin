package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class NBTUtilsBase implements NBTUtils {
    protected final Platform platform;

    protected NBTUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
