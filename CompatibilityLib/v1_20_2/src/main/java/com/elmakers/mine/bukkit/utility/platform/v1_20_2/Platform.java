package com.elmakers.mine.bukkit.utility.platform.v1_20_2;

import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.event.EntityLoadEventHandler;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.event.ResourcePackListener;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.listener.EntityPickupListener;

public class Platform extends PlatformBase {
    private Boolean hasEntityLoadEvent;

    public Platform(MageController controller) {
        super(controller);
    }

    @Override
    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new PersistentEntityMetadataUtils(this.getPlugin());
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.SchematicUtils createSchematicUtils() {
        return new SchematicUtils(this);
    }

    @Override
    protected DeprecatedUtils createDeprecatedUtils() {
        return new DeprecatedUtils(this);
    }

    @Override
    public boolean hasChatComponents() {
        return true;
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.NBTUtils createNBTUtils() {
        return new NBTUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.ItemUtils createItemUtils() {
        return new ItemUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.InventoryUtils createInventoryUtils() {
        return new InventoryUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }

    @Override
    protected MobUtils createMobUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.v1_20_2.MobUtils(this);
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
    protected SkinUtils createSkinUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.v1_20_2.SkinUtils(this);
    }

    @Override
    public boolean hasDeferredEntityLoad() {
        return true;
    }

    @Override
    public void registerEvents(PluginManager pm) {
        super.registerEvents(pm);
        ResourcePackListener rpListener = new ResourcePackListener(controller);
        pm.registerEvents(rpListener, controller.getPlugin());
        if (hasEntityLoadEvent()) {
            pm.registerEvents(new EntityLoadEventHandler(controller), controller.getPlugin());
        }
    }

    @Override
    protected void registerPickupEvent(PluginManager pm) {
        pm.registerEvents(new EntityPickupListener(controller), controller.getPlugin());
    }
}
