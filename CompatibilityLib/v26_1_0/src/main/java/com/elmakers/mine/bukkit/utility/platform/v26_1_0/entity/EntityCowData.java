package com.elmakers.mine.bukkit.utility.platform.v26_1_0.entity;

import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityCowData extends EntityAnimalData {
    private Cow.Variant variant;

    public EntityCowData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            Registry<Cow.Variant> registry = controller.getPlugin().getServer().getRegistry(Cow.Variant.class);
            variant = registry.get(namespacedKey);
        }
    }

    public EntityCowData(Entity entity) {
        super(entity);
        if (entity instanceof Cow) {
            Cow cow = (Cow)entity;
            variant = cow.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Cow) {
            Cow cow = (Cow)entity;
            if (variant != null) cow.setVariant(variant);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Cow cow = (Cow)entity;
        cow.setVariant(cycleRegistryValue(cow.getVariant(), entity.getServer().getRegistry(Cow.Variant.class)));

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Cow;
    }
}
