package com.elmakers.mine.bukkit.utility.platform.modern2;

import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernPlatform;
import com.elmakers.mine.bukkit.utility.platform.modern2.event.EntityLoadEventHandler;

public abstract class Modern2Platform extends ModernPlatform {
    private Boolean hasEntityLoadEvent;

    public Modern2Platform(MageController controller) {
        super(controller);
    }

    @Override
    protected SkinUtils createSkinUtils() {
        return new Modern2SkinUtils(this);
    }

    @Override
    protected SchematicUtils createSchematicUtils() {
        return new Modern2SchematicUtils(this);
    }

    @Override
    protected DeprecatedUtils createDeprecatedUtils() {
        return new Modern2DeprecatedUtils(this);
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

    @Override
    public boolean hasDeferredEntityLoad() {
        return true;
    }

    @Override
    public void registerEvents(PluginManager pm) {
        super.registerEvents(pm);
        if (hasEntityLoadEvent()) {
            pm.registerEvents(new EntityLoadEventHandler(controller), controller.getPlugin());
        }
    }
}
