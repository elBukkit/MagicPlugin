package com.elmakers.mine.bukkit.utility.platform.v1_16.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PiglinAbstract;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityAbstractPiglinData extends com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityFoxData {
    private Boolean transformable;

    public EntityAbstractPiglinData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        transformable = ConfigUtils.getOptionalBoolean(parameters, "transformable");
    }

    public EntityAbstractPiglinData(Entity entity) {
        super(entity);
        if (entity instanceof PiglinAbstract) {
            PiglinAbstract piglin = (PiglinAbstract)entity;
            transformable = !piglin.isImmuneToZombification();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof PiglinAbstract) {
            PiglinAbstract piglin = (PiglinAbstract)entity;
            if (transformable != null) {
                piglin.setImmuneToZombification(!transformable);
            }
        }
    }
}
