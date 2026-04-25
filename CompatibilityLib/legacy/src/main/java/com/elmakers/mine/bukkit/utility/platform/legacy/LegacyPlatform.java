package com.elmakers.mine.bukkit.utility.platform.legacy;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.base.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public class LegacyPlatform extends PlatformBase {

    public LegacyPlatform(MageController controller) {
        super(controller);
    }

    @Override
    protected boolean initialize() {
        return com.elmakers.mine.bukkit.utility.platform.base.NMSUtils.initialize(this);
    }

    @Override
    public boolean isLegacy() {
        return com.elmakers.mine.bukkit.utility.platform.base.NMSUtils.legacy;
    }

    @Override
    public boolean isCurrentVersion() {
        return com.elmakers.mine.bukkit.utility.platform.base.NMSUtils.isModernVersion;
    }

    @Override
    public boolean hasStatistics() {
        return com.elmakers.mine.bukkit.utility.platform.base.NMSUtils.hasStatistics;
    }

    @Override
    public boolean hasEntityTransformEvent() {
        return com.elmakers.mine.bukkit.utility.platform.base.NMSUtils.hasEntityTransformEvent;
    }

    @Override
    public boolean hasTimeSkipEvent() {
        return com.elmakers.mine.bukkit.utility.platform.base.NMSUtils.hasTimeSkipEvent;
    }

    @Override
    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new LegacyEntityMetadataUtils(this.getPlugin());
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.SkinUtils createSkinUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.base.SkinUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.SchematicUtils createSchematicUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.base.SchematicUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.NBTUtils createNBTUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.base.NBTUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.ItemUtils createItemUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.base.ItemUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.InventoryUtils createInventoryUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.base.InventoryUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils createCompatibilityUtils() {
        return new com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils createDeprecatedUtils() {
        return new DeprecatedUtils(this);
    }
}
