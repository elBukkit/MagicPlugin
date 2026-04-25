package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PiglinAbstract;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityAbstractPiglinData extends EntityExtraData {
    private Boolean transformable;

    public EntityAbstractPiglinData(ConfigurationSection parameters, MageController controller) {
        transformable = ConfigUtils.getOptionalBoolean(parameters, "transformable");
    }

    public EntityAbstractPiglinData(Entity entity) {
        if (entity instanceof PiglinAbstract) {
            PiglinAbstract piglin = (PiglinAbstract)entity;
            transformable = !piglin.isImmuneToZombification();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof PiglinAbstract) {
            PiglinAbstract piglin = (PiglinAbstract)entity;
            if (transformable != null) {
                piglin.setImmuneToZombification(!transformable);
            }
        }
    }
}
