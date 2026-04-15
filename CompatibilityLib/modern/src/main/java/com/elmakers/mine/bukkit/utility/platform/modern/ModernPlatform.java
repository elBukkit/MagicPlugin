package com.elmakers.mine.bukkit.utility.platform.modern;

import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;
import com.elmakers.mine.bukkit.utility.platform.v1_12.listener.EntityPickupListener;
import com.elmakers.mine.bukkit.utility.platform.v1_16.PersistentEntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.v1_16.event.ResourcePackListener;

public abstract class ModernPlatform extends PlatformBase {

    public ModernPlatform(MageController controller) {
        super(controller);
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
    public void registerEvents(PluginManager pm) {
        super.registerEvents(pm);
        ResourcePackListener timeListener = new ResourcePackListener(controller);
        pm.registerEvents(timeListener, controller.getPlugin());
    }

    @Override
    protected void registerPickupEvent(PluginManager pm) {
        pm.registerEvents(new EntityPickupListener(controller), controller.getPlugin());
    }

    @Override
    public boolean hasChatComponents() {
        return true;
    }
}
