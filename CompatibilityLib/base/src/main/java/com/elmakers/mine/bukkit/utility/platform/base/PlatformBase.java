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
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.PaperUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public abstract class PlatformBase implements Platform {
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
    protected final EntityUtils entityUtils;
    protected final PaperUtils paperUtils;
    @Nonnull
    protected final EntityMetadataUtils entityMetadataUtils;
    protected final boolean valid;

    public PlatformBase(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
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
            this.entityMetadataUtils = createEntityMetadataUtils();
            this.entityUtils = createEntityUtils();
        } else {
            this.compatibilityUtils = null;
            this.deprecatedUtils = null;
            this.inventoryUtils = null;
            this.itemUtils = null;
            this.nbtUtils = null;
            this.schematicUtils = null;
            this.skinUtils = null;
            this.paperUtils = null;
            this.entityMetadataUtils = null;
            this.entityUtils = null;
        }
    }

    protected boolean initialize() {
        return true;
    }

    public void registerEvents(MageController controller, PluginManager pm) {
        if (paperUtils != null) {
            paperUtils.registerEvents(controller, pm);
        }
    }

    protected EntityMetadataUtils createEntityMetadataUtils() {
        throw new IllegalStateException("Platform does not implement createEntityMetadataUtils");
    }

    protected EntityUtils createEntityUtils() {
        throw new IllegalStateException("Platform does not implement createEntityUtils");
    }

    protected PaperUtils createPaperUtils() {
        // Is there a better way to check for Paper?
        try {
            World.class.getMethod("getChunkAtAsync", Integer.TYPE, Integer.TYPE, Boolean.TYPE, Consumer.class);
            logger.info("Async chunk loading API found");
            return new com.elmakers.mine.bukkit.utility.paper.PaperUtils(this);
        } catch (Throwable ignore) {
        }
        // null PaperUtils is OK
        return paperUtils;
    }

    protected SkinUtils createSkinUtils() {
        throw new IllegalStateException("Platform does not implement createSkinUtils");
    }

    protected SchematicUtils createSchematicUtils() {
        throw new IllegalStateException("Platform does not implement createSchematicUtils");
    }

    protected NBTUtils createNBTUtils() {
        throw new IllegalStateException("Platform does not implement createNBTUtils");
    }

    protected ItemUtils createItemUtils() {
        throw new IllegalStateException("Platform does not implement createItemUtils");
    }

    protected InventoryUtils createInventoryUtils() {
        throw new IllegalStateException("Platform does not implement createInventoryUtils");
    }

    protected CompatibilityUtils createCompatibilityUtils() {
        throw new IllegalStateException("Platform does not implement createCompatibilityUtils");
    }

    protected DeprecatedUtils createDeprecatedUtils() {
        throw new IllegalStateException("Platform does not implement createDeprecatedUtils");
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
    public EntityMetadataUtils getEnityMetadataUtils() {
        return entityMetadataUtils;
    }

    @Override
    public EntityUtils getEntityUtils() {
        return entityUtils;
    }
}
