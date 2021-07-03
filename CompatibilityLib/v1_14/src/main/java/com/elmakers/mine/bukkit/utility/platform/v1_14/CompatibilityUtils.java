package com.elmakers.mine.bukkit.utility.platform.v1_14;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
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

    @SuppressWarnings("deprecation")
    protected RecipeChoice getChoice(ItemStack item, boolean ignoreDamage) {
        RecipeChoice.ExactChoice exactChoice;
        short maxDurability = item.getType().getMaxDurability();
        if (ignoreDamage && maxDurability > 0) {
            List<ItemStack> damaged = new ArrayList<>();
            for (short damage = 0; damage < maxDurability; damage++) {
                item = item.clone();
                ItemMeta meta = item.getItemMeta();
                if (meta == null || !(meta instanceof org.bukkit.inventory.meta.Damageable))  break;
                org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable)meta;
                damageable.setDamage(damage);
                item.setItemMeta(meta);
                damaged.add(item);
            }
            // Not really deprecated, just a draft API at this point but it works.
            exactChoice = new RecipeChoice.ExactChoice(damaged);
        } else {
            exactChoice = new RecipeChoice.ExactChoice(item);
        }
        return exactChoice;
    }

    @Override
    public FurnaceRecipe createFurnaceRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new FurnaceRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating furnace recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createBlastingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new BlastingRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating blasting recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createCampfireRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new CampfireRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating campfire recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createSmokingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new SmokingRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating smoking recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createStonecuttingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new StonecuttingRecipe(namespacedKey, item, choice);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating stonecutton recipe", ex);
        }
        return null;
    }

    @Override
    public boolean setRecipeGroup(Recipe recipe, String group) {
        if (recipe instanceof ShapedRecipe) {
            ((ShapedRecipe)recipe).setGroup(group);
            return true;
        }
        if (recipe instanceof CookingRecipe) {
            ((CookingRecipe)recipe).setGroup(group);
            return true;
        }
        return false;
    }
}
