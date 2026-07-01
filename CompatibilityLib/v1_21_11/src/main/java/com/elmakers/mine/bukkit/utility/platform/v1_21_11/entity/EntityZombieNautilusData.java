package com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity;

import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ZombieNautilus;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityAnimalData;

public class EntityZombieNautilusData extends EntityAnimalData {
    private ZombieNautilus.Variant variant;

    public EntityZombieNautilusData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            Registry<ZombieNautilus.Variant> registry = controller.getPlugin().getServer().getRegistry(ZombieNautilus.Variant.class);
            variant = registry.get(namespacedKey);
        }
    }

    public EntityZombieNautilusData(Entity entity) {
        super(entity);
        if (entity instanceof ZombieNautilus) {
            ZombieNautilus zombieNautilus = (ZombieNautilus)entity;
            variant = zombieNautilus.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof ZombieNautilus) {
            ZombieNautilus zombieNautilus = (ZombieNautilus)entity;
            if (variant != null) zombieNautilus.setVariant(variant);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        ZombieNautilus zombieNautilus = (ZombieNautilus)entity;
        zombieNautilus.setVariant(cycleRegistryValue(zombieNautilus.getVariant(), entity.getServer().getRegistry(ZombieNautilus.Variant.class)));

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof ZombieNautilus;
    }
}
