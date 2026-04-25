package com.elmakers.mine.bukkit.utility.platform.legacy;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.DeprecatedUtilsBase;

/**
 * Makes deprecation warnings useful again by suppressing all bukkit 'magic
 * number' deprecations.
 *
 */
@SuppressWarnings("deprecation")
public class DeprecatedUtils extends DeprecatedUtilsBase {
    public DeprecatedUtils(Platform platform) {
        super(platform);
    }

}
