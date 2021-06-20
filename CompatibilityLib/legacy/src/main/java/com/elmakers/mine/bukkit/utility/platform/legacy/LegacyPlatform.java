package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public class LegacyPlatform extends PlatformBase {

    public LegacyPlatform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected boolean initialize() {
        return NMSUtils.initialize(this);
    }

    @Override
    public boolean isLegacy() {
        return NMSUtils.legacy;
    }

    @Override
    public boolean isCurrentVersion() {
        return NMSUtils.isModernVersion;
    }

    @Override
    public boolean hasStatistics() {
        return NMSUtils.hasStatistics;
    }

    @Override
    public boolean hasEntityTransformEvent() {
        return NMSUtils.hasEntityTransformEvent;
    }

    @Override
    public boolean hasTimeSkipEvent() {
        return NMSUtils.hasTimeSkipEvent;
    }

    @Override
    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new LegacyEntityMetadataUtils(this.getPlugin());
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.SkinUtils createSkinUtils() {
        return new SkinUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.SchematicUtils createSchematicUtils() {
        return new SchematicUtils(this);
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
    protected com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils createDeprecatedUtils() {
        return new DeprecatedUtils(this);
    }
}
