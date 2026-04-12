package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.PaperUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.SpigotUtils;
import com.elmakers.mine.bukkit.utility.platform.base.event.EntityLoadEventHandler;
import com.elmakers.mine.bukkit.utility.platform.base.event.EntityPickupListener;
import com.elmakers.mine.bukkit.utility.platform.base.event.ResourcePackListener;

public abstract class PlatformBase implements Platform {
    protected final MageController controller;
    private final Logger logger;
    private final Plugin plugin;
    @Nonnull
    protected final CompatibilityUtils compatibilityUtils;
    @Nonnull
    protected final DeprecatedUtils deprecatedUtils;
    @Nonnull
    protected final InventoryUtils inventoryUtils;
    @Nonnull
    protected final ItemUtils itemUtils;
    @Nonnull
    protected final NBTUtils nbtUtils;
    @Nonnull
    protected final SchematicUtils schematicUtils;
    @Nonnull
    protected final SkinUtils skinUtils;
    @Nonnull
    protected final MobUtils mobUtils;
    @Nonnull
    protected final EntityUtils entityUtils;
    protected final PaperUtils paperUtils;
    protected final SpigotUtils spigotUtils;
    @Nonnull
    protected final EntityMetadataUtils entityMetadataUtils;
    private Boolean hasEntityLoadEvent;
    protected final boolean valid;

    public PlatformBase(MageController controller) {
        this.controller = controller;
        this.plugin = controller.getPlugin();
        this.logger = controller.getLogger();
        this.valid = initialize();

        if (valid) {
            this.compatibilityUtils = createCompatibilityUtils();
            this.deprecatedUtils = createDeprecatedUtils();
            this.inventoryUtils = createInventoryUtils();
            this.itemUtils = createItemUtils();
            this.nbtUtils = createNBTUtils();
            this.schematicUtils = createSchematicUtils();
            this.skinUtils = createSkinUtils();
            this.paperUtils = createPaperUtils();
            this.spigotUtils = createSpigotUtils();
            this.entityMetadataUtils = createEntityMetadataUtils();
            this.entityUtils = createEntityUtils();
            this.mobUtils = createMobUtils();
        } else {
            this.compatibilityUtils = null;
            this.deprecatedUtils = null;
            this.inventoryUtils = null;
            this.itemUtils = null;
            this.nbtUtils = null;
            this.schematicUtils = null;
            this.skinUtils = null;
            this.paperUtils = null;
            this.spigotUtils = null;
            this.entityMetadataUtils = null;
            this.entityUtils = null;
            this.mobUtils = null;
        }
    }

    protected boolean initialize() {
        return true;
    }

    @Override
    public void registerEvents(PluginManager pm) {
        if (paperUtils != null) {
            paperUtils.registerEvents(controller, pm);
        }
        pm.registerEvents(new EntityPickupListener(controller), controller.getPlugin());

        ResourcePackListener timeListener = new ResourcePackListener(controller);
        pm.registerEvents(timeListener, controller.getPlugin());

        if (hasEntityLoadEvent()) {
            pm.registerEvents(new EntityLoadEventHandler(controller), controller.getPlugin());
        }
    }

    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new PersistentEntityMetadataUtils(this.getPlugin());
    }

    protected EntityUtils createEntityUtils() {
        return new EntityUtilsBase(this);
    }

    protected PaperUtils createPaperUtils() {
        // Is there a better way to check for Paper?
        try {
            World.class.getMethod("getChunkAtAsync", Integer.TYPE, Integer.TYPE, Boolean.TYPE, Consumer.class);
            logger.info("Async chunk loading API found");
            return new com.elmakers.mine.bukkit.utility.paper.PaperUtils();
        } catch (Throwable ignore) {
        }
        // null PaperUtils is OK
        return paperUtils;
    }

    protected SpigotUtils createSpigotUtils() {
        // Is there a better way to check for Spigot?
        try {
            // We currently only use Spigot for chat component support, but maybe in the future
            // we will need to look more closely
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> bungeeColor = Class.forName("net.md_5.bungee.api.ChatColor");
            bungeeColor.getMethod("of", String.class);
            logger.info("Chat component API found");
            return new spigot.SpigotUtils(this);
        } catch (Throwable ignore) {
        }
        // null SpigotUtils is OK
        return spigotUtils;
    }

    protected SkinUtils createSkinUtils() {
        return new SkinUtilsBase(this);
    }

    protected SchematicUtils createSchematicUtils() {
        return new SchematicUtilsBase(this);
    }

    protected NBTUtils createNBTUtils() {
        throw new IllegalStateException("Platform does not implement createNBTUtils");
    }

    protected ItemUtils createItemUtils() {
        throw new IllegalStateException("Platform does not implement createItemUtils");
    }

    protected InventoryUtils createInventoryUtils() {
        return new InventoryUtilsBase(this);
    }

    protected CompatibilityUtils createCompatibilityUtils() {
        throw new IllegalStateException("Platform does not implement createCompatibilityUtils");
    }

    protected DeprecatedUtils createDeprecatedUtils() {
        return new DeprecatedUtilsBase(this);
    }

    protected MobUtils createMobUtils() {
        return new MobUtilsBase();
    }

    @Override
    public MageController getController() {
        return controller;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public boolean isCurrentVersion() {
        return true;
    }

    @Override
    public boolean hasStatistics() {
        return true;
    }

    @Override
    public boolean hasEntityTransformEvent() {
        return true;
    }

    @Override
    public boolean hasTimeSkipEvent() {
        return true;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public CompatibilityUtils getCompatibilityUtils() {
        return compatibilityUtils;
    }

    @Override
    public DeprecatedUtils getDeprecatedUtils() {
        return deprecatedUtils;
    }

    @Override
    public InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }

    @Override
    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    @Override
    public NBTUtils getNBTUtils() {
        return nbtUtils;
    }

    @Override
    public SchematicUtils getSchematicUtils() {
        return schematicUtils;
    }

    @Override
    public SkinUtils getSkinUtils() {
        return skinUtils;
    }

    @Override
    @Nullable
    public PaperUtils getPaperUtils() {
        return paperUtils;
    }

    @Override
    @Nullable
    public SpigotUtils getSpigotUtils() {
        return spigotUtils;
    }

    @Override
    public EntityMetadataUtils getEnityMetadataUtils() {
        return entityMetadataUtils;
    }

    @Override
    public EntityUtils getEntityUtils() {
        return entityUtils;
    }

    @Override
    public MobUtils getMobUtils() {
        return mobUtils;
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
