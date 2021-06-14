package base;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public abstract class PlatformBase implements Platform {
    private final Logger logger;
    private final Plugin plugin;
    protected boolean valid = false;
    protected CompatibilityUtils compatibilityUtils;
    protected DeprecatedUtils deprecatedUtils;
    protected InventoryUtils inventoryUtils;
    protected ItemUtils itemUtils;
    protected NBTUtils nbtUtils;
    protected SchematicUtils schematicUtils;
    protected SkinUtils skinUtils;

    public PlatformBase(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
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
    public String getVersionPrefix() {
        return null;
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
    
}
