package com.elmakers.mine.bukkit.utility.platform.modern2;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernDeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernPlatform;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernSchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernSkinUtils;

public abstract class Modern2Platform extends ModernPlatform {
    public Modern2Platform(MageController controller) {
        super(controller);
    }

    @Override
    protected SkinUtils createSkinUtils() {
        return new ModernSkinUtils(this);
    }

    @Override
    protected SchematicUtils createSchematicUtils() {
        return new ModernSchematicUtils(this);
    }

    @Override
    protected DeprecatedUtils createDeprecatedUtils() {
        return new ModernDeprecatedUtils(this);
    }
}
