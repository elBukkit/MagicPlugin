package com.elmakers.mine.bukkit.utility.platform.base_v26_1.entity;

import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityFrogData extends EntityAnimalData {
    private Frog.Variant variant;

    public EntityFrogData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            Registry<Frog.Variant> registry = controller.getPlugin().getServer().getRegistry(Frog.Variant.class);
            variant = registry.get(namespacedKey);
        }
    }

    public EntityFrogData(Entity entity) {
        super(entity);
        if (entity instanceof Frog) {
            Frog frog = (Frog)entity;
            variant = frog.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Frog) {
            Frog frog = (Frog)entity;
            if (variant != null) frog.setVariant(variant);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Frog frog = (Frog)entity;
        frog.setVariant(cycleRegistryValue(frog.getVariant(), entity.getServer().getRegistry(Frog.Variant.class)));

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Frog;
    }
}
