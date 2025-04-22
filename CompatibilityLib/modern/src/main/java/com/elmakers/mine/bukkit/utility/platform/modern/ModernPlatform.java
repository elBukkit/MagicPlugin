package com.elmakers.mine.bukkit.utility.platform.modern;

import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;
import com.elmakers.mine.bukkit.utility.platform.modern.event.EntityLoadEventHandler;
import com.elmakers.mine.bukkit.utility.platform.modern.event.EntityPickupListener;
import com.elmakers.mine.bukkit.utility.platform.modern.event.ResourcePackListener;

public abstract class ModernPlatform extends PlatformBase {
    private Boolean hasEntityLoadEvent;

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

        if (hasEntityLoadEvent()) {
            pm.registerEvents(new EntityLoadEventHandler(controller), controller.getPlugin());
        }
    }

    @Override
    protected void registerPickupEvent(PluginManager pm) {
        pm.registerEvents(new EntityPickupListener(controller), controller.getPlugin());
    }

    @Override
    public boolean hasChatComponents() {
        return true;
    }

    @Override
    public boolean hasDeferredEntityLoad() {
        return true;
    }

    @Override
    public boolean hasEntityLoadEvent() {
        if (hasEntityLoadEvent == null) {
            try {
                Class.forName("org.bukkit.event.world.EntitiesLoadEvent");
                hasEntityLoadEvent = true;
            } catch (Exception ex) {
                hasEntityLoadEvent = false;
                getLogger().warning("EntitiesLoadEvent not found, it is recommended that you update your server software");
            }
        }
        return hasEntityLoadEvent;
    }
}
