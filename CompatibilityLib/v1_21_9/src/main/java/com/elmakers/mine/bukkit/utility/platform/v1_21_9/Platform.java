package com.elmakers.mine.bukkit.utility.platform.v1_21_9;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public class Platform extends PlatformBase {

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
    protected com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.MobUtils createMobUtils() {
        return new MobUtils(this);
    }
}
