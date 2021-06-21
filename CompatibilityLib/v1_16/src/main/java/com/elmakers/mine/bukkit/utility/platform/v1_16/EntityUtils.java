package com.elmakers.mine.bukkit.utility.platform.v1_16;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityCatData;
import com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityFoxData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_14.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        EntityExtraData extraData = null;
        // We have an overloaded fox data here, which includes the newer trust API
        if (entity.getType() == EntityType.FOX) {
            extraData = new EntityFoxData(entity);
        }
        if (extraData == null) {
            extraData = super.getExtraData(controller, entity);
        }
        return extraData;
    }
}
