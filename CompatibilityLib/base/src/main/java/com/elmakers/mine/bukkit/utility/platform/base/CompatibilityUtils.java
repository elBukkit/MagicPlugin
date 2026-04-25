package com.elmakers.mine.bukkit.utility.platform.base;

import com.elmakers.mine.bukkit.utility.platform.Platform;

/**
 * A generic place to put compatibility-based utilities.
 *
 * <p>These are generally here when there is a new method added
 * to the Bukkti API we'd like to use, but aren't quite
 * ready to give up backwards compatibility.
 *
 * <p>The easy solution to this problem is to shamelessly copy
 * Bukkit's code in here, mark it as deprecated and then
 * switch everything over once the new Bukkit method is in an
 * official release.
 */
@SuppressWarnings("deprecation")
public class CompatibilityUtils extends CompatibilityUtilsBase {

    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

}
