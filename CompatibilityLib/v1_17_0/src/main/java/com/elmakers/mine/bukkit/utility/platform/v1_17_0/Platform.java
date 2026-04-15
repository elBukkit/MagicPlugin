package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernPlatform;

public class Platform extends ModernPlatform {

    public Platform(MageController controller) {
        super(controller);
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
    public boolean hasDeferredEntityLoad() {
        return true;
    }
}
