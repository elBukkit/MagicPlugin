package com.elmakers.mine.bukkit.utility.platform.v26_1_1.entity;

import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityChickenData extends EntityAnimalData {
    private Chicken.Variant variant;

    public EntityChickenData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            Registry<Chicken.Variant> registry = controller.getPlugin().getServer().getRegistry(Chicken.Variant.class);
            variant = registry.get(namespacedKey);
        }
    }

    public EntityChickenData(Entity entity) {
        super(entity);
        if (entity instanceof Chicken) {
            Chicken chicken = (Chicken)entity;
            variant = chicken.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Chicken) {
            Chicken chicken = (Chicken)entity;
            if (variant != null) chicken.setVariant(variant);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Chicken chicken = (Chicken)entity;
        chicken.setVariant(cycleRegistryValue(chicken.getVariant(), entity.getServer().getRegistry(Chicken.Variant.class)));

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Chicken;
    }
}
