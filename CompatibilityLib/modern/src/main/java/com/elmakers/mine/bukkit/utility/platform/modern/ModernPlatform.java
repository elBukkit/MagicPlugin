package com.elmakers.mine.bukkit.utility.platform.modern;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public abstract class ModernPlatform extends PlatformBase {
    public ModernPlatform(MageController controller) {
        super(controller);
    }

    @Override
    protected SchematicUtils createSchematicUtils() {
        return new ModernSchematicUtils(this);
    }
}
