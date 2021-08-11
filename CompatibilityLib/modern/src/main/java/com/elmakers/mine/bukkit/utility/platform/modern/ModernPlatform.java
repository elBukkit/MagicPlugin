package com.elmakers.mine.bukkit.utility.platform.modern;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public abstract class ModernPlatform extends PlatformBase {

    public ModernPlatform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new PersistentEntityMetadataUtils(this.getPlugin());
    }

    @Override
    protected SkinUtils createSkinUtils() {
        return new ModernSkinUtils(this);
    }

    @Override
    protected SchematicUtils createSchematicUtils() {
        return new ModernSchematicUtils(this);
    }
}
