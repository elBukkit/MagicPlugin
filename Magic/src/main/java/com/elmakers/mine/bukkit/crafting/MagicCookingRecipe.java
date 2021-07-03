package com.elmakers.mine.bukkit.crafting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public abstract class MagicCookingRecipe extends MagicRecipe {
    protected Recipe recipe;
    protected ItemData ingredient;
    protected String group;
    protected float experience;
    protected int cookingTime;

    protected MagicCookingRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    public ItemStack load(ConfigurationSection configuration) {
        ItemStack item = super.load(configuration);
        if (item == null) {
            return null;
        }
        cookingTime = configuration.getInt("cooking_time") / 50;
        experience = (float)configuration.getDouble("experience");
        String materialKey = configuration.getString("ingredient");
        ingredient = controller.getOrCreateItem(materialKey);
        if (ingredient == null) {
            controller.getLogger().warning("Could not create " + getType() + " recipe ingredient: " + materialKey);
            return null;
        }
        recipe = createRecipe(item);
        if (recipe != null && group != null && !group.isEmpty()) {
            CompatibilityLib.getCompatibilityUtils().setRecipeGroup(recipe, group);
        }
        return item;
    }

    protected abstract Recipe createRecipe(ItemStack item);

    @Override
    public Recipe getRecipe() {
        return recipe;
    }
}
