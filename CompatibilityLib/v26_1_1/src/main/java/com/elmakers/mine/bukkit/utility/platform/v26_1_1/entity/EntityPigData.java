package com.elmakers.mine.bukkit.utility.platform.v26_1_1.entity;

import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityPigData extends EntityAnimalData {
    private Pig.Variant variant;

    public EntityPigData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            Registry<Pig.Variant> registry = controller.getPlugin().getServer().getRegistry(Pig.Variant.class);
            variant = registry.get(namespacedKey);
        }
    }

    public EntityPigData(Entity entity) {
        super(entity);
        if (entity instanceof Pig) {
            Pig pig = (Pig)entity;
            variant = pig.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Pig) {
            Pig pig = (Pig)entity;
            if (variant != null) pig.setVariant(variant);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Pig pig = (Pig)entity;
        pig.setVariant(cycleRegistryValue(pig.getVariant(), entity.getServer().getRegistry(Pig.Variant.class)));

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Pig;
    }
}
