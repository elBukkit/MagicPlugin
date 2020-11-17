package com.elmakers.mine.bukkit.entity;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityParrotData extends EntityExtraData {
    public Parrot.Variant variant;

    public EntityParrotData() {

    }

    public EntityParrotData(ConfigurationSection parameters, MageController controller) {
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
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot)entity;
            variant = parrot.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot)entity;
            if (variant != null) {
                parrot.setVariant(variant);
            }
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityParrotData copy = new EntityParrotData();
        copy.variant = variant;
        return copy;
    }
}
