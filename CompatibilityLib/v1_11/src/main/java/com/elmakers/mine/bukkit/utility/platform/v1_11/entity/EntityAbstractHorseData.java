package com.elmakers.mine.bukkit.utility.platform.v1_11.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityAbstractHorseData extends com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAbstractHorseData {
    public EntityAbstractHorseData() {

    }

    public EntityAbstractHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
    }

    public EntityAbstractHorseData(Entity entity) {
        super(entity);
        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)entity;
            domestication = horse.getDomestication();
            maxDomestication = horse.getMaxDomestication();
            jumpStrength = horse.getJumpStrength();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)entity;
            if (domestication != null) {
                horse.setDomestication(domestication);
            }
            if (maxDomestication != null) {
                horse.setMaxDomestication(maxDomestication);
            }
            if (jumpStrength != null) {
                horse.setJumpStrength(jumpStrength);
            }
        }
    }
}
