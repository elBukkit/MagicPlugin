package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;
import com.elmakers.mine.bukkit.utility.platform.modern.PersistentEntityMetadataUtils;

public class Platform extends PlatformBase {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new PersistentEntityMetadataUtils(this.getPlugin());
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

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }
}
