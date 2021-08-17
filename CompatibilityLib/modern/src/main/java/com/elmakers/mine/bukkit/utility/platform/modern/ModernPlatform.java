package com.elmakers.mine.bukkit.utility.platform.modern;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;
import com.elmakers.mine.bukkit.utility.platform.v1_16.PersistentEntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.v1_16.event.ResourcePackListener;

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

    @Override
    protected DeprecatedUtils createDeprecatedUtils() {
        return new ModernDeprecatedUtils(this);
    }

    @Override
    public void registerEvents(MageController controller, PluginManager pm) {
        super.registerEvents(controller, pm);
        ResourcePackListener timeListener = new ResourcePackListener(controller);
        pm.registerEvents(timeListener, controller.getPlugin());
    }
}
