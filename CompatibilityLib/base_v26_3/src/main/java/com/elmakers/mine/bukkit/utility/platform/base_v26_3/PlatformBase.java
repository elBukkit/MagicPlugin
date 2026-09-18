package com.elmakers.mine.bukkit.utility.platform.base_v26_3;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;

public abstract class PlatformBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_2.PlatformBase {

    public PlatformBase(MageController controller) {
        super(controller);
    }

    @Override
    protected EntityUtils createEntityUtils() {
        return new EntityUtilsBase(this);
    }

    @Override
    protected ItemUtils createItemUtils() {
        return new ItemUtilsBase(this);
    }

    @Override
    protected CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtilsBase(this);
    }

    @Override
    protected MobUtils createMobUtils() {
        return new MobUtilsBase(this);
    }
}
