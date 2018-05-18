package com.elmakers.mine.bukkit.entity;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class EntityParrotData extends EntityExtraData {
    public Enum<?> variant;

    public EntityParrotData() {

    }

    public EntityParrotData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String variantName = parameters.getString("parrot_variant");
        if (variantName != null && !variantName.isEmpty()) {
            variant = CompatibilityUtils.getParrotVariant(variantName);
            if (variant == null) {
                log.warning("Invalid parrot variant: " + variantName);
            }
        }
    }

    public EntityParrotData(Entity entity) {
       variant = CompatibilityUtils.getParrotVariant(entity);
    }

    @Override
    public void apply(Entity entity) {
        if (variant != null) {
            CompatibilityUtils.setParrotVariant(entity, variant);
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityParrotData copy = new EntityParrotData();
        copy.variant = variant;
        return copy;
    }
}
