package com.elmakers.mine.bukkit.utility.platform.v1_20_3;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base_v1_17_0.SkinUtilsBase;
import com.mojang.authlib.properties.Property;

public class SkinUtils extends SkinUtilsBase {

    public SkinUtils(Platform platform) {
        super(platform);
    }

    @Override
    protected String getValue(Property property) {
        return property.value();
    }

    @Override
    protected String getSignature(Property property) {
        return property.signature();
    }
}
