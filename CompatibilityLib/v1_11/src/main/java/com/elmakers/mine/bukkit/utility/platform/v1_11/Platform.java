package com.elmakers.mine.bukkit.utility.platform.v1_11;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_10.Platform {

    public Platform(MageController controller) {
        super(controller);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }
}
