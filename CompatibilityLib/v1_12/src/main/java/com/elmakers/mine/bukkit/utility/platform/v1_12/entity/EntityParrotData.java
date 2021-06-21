package com.elmakers.mine.bukkit.utility.platform.v1_12.entity;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityParrotData extends EntityAnimalData {
    public Parrot.Variant variant;

    public EntityParrotData() {

    }

    public EntityParrotData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String variantName = parameters.getString("parrot_variant");
        if (variantName != null && !variantName.isEmpty()) {
            try {
                variant = Parrot.Variant.valueOf(variantName.toUpperCase());
            } catch (Exception ex) {
                log.warning("Invalid parrot variant: " + variantName);
            }
        }
    }

    public EntityParrotData(Entity entity) {
        super(entity);
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot)entity;
            variant = parrot.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot)entity;
            if (variant != null) {
                parrot.setVariant(variant);
            }
        }
    }
}
