package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;

public abstract class SchematicUtilsBase implements SchematicUtils {
    protected final Platform platform;

    protected SchematicUtilsBase(final Platform platform) {
        this.platform = platform;
    }
}
