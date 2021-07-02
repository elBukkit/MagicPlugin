package com.elmakers.mine.bukkit.utility.platform.v1_14;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class CompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_13.CompatibilityUtils {

    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public boolean isArrow(Entity projectile) {
        return projectile instanceof AbstractArrow;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FurnaceRecipe createFurnaceRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice.ExactChoice exactChoice;
            short maxDurability = source.getType().getMaxDurability();
            if (ignoreDamage && maxDurability > 0) {
                List<ItemStack> damaged = new ArrayList<>();
                for (short damage = 0; damage < maxDurability; damage++) {
                    source = source.clone();
                    ItemMeta meta = source.getItemMeta();
                    if (meta == null || !(meta instanceof org.bukkit.inventory.meta.Damageable))  break;
                    org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable)meta;
                    damageable.setDamage(damage);
                    source.setItemMeta(meta);
                    damaged.add(source);
                }
                // Not really deprecated, just a draft API at this point but it works.
                exactChoice = new RecipeChoice.ExactChoice(damaged);
            } else {
                exactChoice = new RecipeChoice.ExactChoice(item);
            }

            return new FurnaceRecipe(namespacedKey, item, exactChoice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating furnace recipe", ex);
        }
        return null;
    }
}
