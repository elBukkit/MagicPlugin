package com.elmakers.mine.bukkit.utility.platform.base_v26_1.entity;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityCowData extends EntityAnimalData {
    // Paper does something annoying here and so we have to hide from the API.
    // The root cause is that we advertise as being a 1.17 plugin for backwards-compatibility,
    // But use 1.26 code.
    // If we ever drop backwards compatibility again, this could be changed back to using the API.
    private Keyed variant;
    private static Class<? extends Keyed> cowClass;
    private static Class<? extends Keyed> cowVariantClass;
    private static Method cowSetVariantMethod;
    private static Method cowGetVariantMethod;
    private static boolean initialized = false;
    private static boolean hasVariant = false;

    private static boolean initialize() {
        if (!initialized) {
            try {
                initialized = true;
                cowClass = (Class<? extends Keyed>)Class.forName("org.bukkit.entity.Cow");
                cowVariantClass = (Class<? extends Keyed>)Class.forName("org.bukkit.entity.Cow$Variant");
                cowSetVariantMethod = cowClass.getDeclaredMethod("setVariant", cowVariantClass);
                cowGetVariantMethod = cowClass.getDeclaredMethod("getVariant");
                hasVariant = true;
            } catch (Throwable ex) {
                Bukkit.getLogger().log(Level.WARNING, "[Magic] Failed to work around Paper doing weird things with Cow.Variant", ex);
            }
        }
        return hasVariant;
    }

    public EntityCowData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        if (!initialize()) return;

        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            Registry<? extends Keyed> registry = controller.getPlugin().getServer().getRegistry(cowVariantClass);
            variant = registry.get(namespacedKey);
        }
    }

    public EntityCowData(Entity entity) {
        super(entity);
        if (!initialize()) return;
        if (entity instanceof Cow) {
            try {
                variant = (Keyed)cowGetVariantMethod.invoke(entity);
            } catch (Throwable ignore) {
            }
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (hasVariant && entity instanceof Cow) {
            if (variant != null) {
                try {
                    cowSetVariantMethod.invoke(entity, variant);
                } catch (Throwable ignore) {
                }
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!hasVariant || !canCycle(entity)) {
            return false;
        }

        // I could not figure out how to call cycleRegistryValue with these generics :(
        try {
            Object currentVariant = cowGetVariantMethod.invoke(entity);
            Object nextVariant = currentVariant;
            Registry<? extends Keyed> registry = entity.getServer().getRegistry(cowVariantClass);
            Iterator<? extends Keyed> it = registry.iterator();
            while (it.hasNext()) {
                Keyed testValue = it.next();
                if (currentVariant == testValue) {
                    if (it.hasNext()) {
                        nextVariant = it.next();
                    } else {
                        nextVariant = registry.iterator().next();
                    }
                    break;
                }
            }

            cowSetVariantMethod.invoke(entity, nextVariant);
        } catch (Throwable ignore) {
        }

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Cow;
    }
}
