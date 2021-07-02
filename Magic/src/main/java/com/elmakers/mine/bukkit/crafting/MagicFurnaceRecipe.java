package com.elmakers.mine.bukkit.crafting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicFurnaceRecipe extends MagicRecipe {
    private FurnaceRecipe recipe;
    private ItemData ingredient;
    private float experience;
    private int cookingTime;

    protected MagicFurnaceRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    protected boolean load(ConfigurationSection configuration) {
        ItemStack item = super.loadItem(configuration);
        if (item == null) {
            return false;
        }
        cookingTime = configuration.getInt("cooking_time") / 50;
        experience = (float)configuration.getDouble("experience");
        String materialKey = configuration.getString("ingredient");
        ingredient = controller.getOrCreateItem(materialKey);
        if (ingredient == null) {
            controller.getLogger().warning("Could not create furnace recipe ingredient: " + materialKey);
            return false;
        }
        recipe = CompatibilityLib.getCompatibilityUtils().createFurnaceRecipe(key, item, ingredient.getItemStack(1), ignoreDamage, experience, cookingTime);
        return true;
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }
}
