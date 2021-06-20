package com.elmakers.mine.bukkit.utility.platform.v1_13;

import org.bukkit.block.Skull;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class DeprecatedUtils extends com.elmakers.mine.bukkit.utility.platform.legacy.DeprecatedUtils {
    public DeprecatedUtils(Platform platform) {
        super(platform);
    }

    @Override
    public void setSkullType(Skull skullBlock, short skullType) {
    }

    @Override
    public short getSkullType(Skull skullBlock) {
        return 0;
    }
}

