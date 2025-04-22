package com.elmakers.mine.bukkit.utility.platform.v1_21_0;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernSkinUtils;
import com.mojang.authlib.properties.Property;

public class SkinUtils extends ModernSkinUtils {

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
