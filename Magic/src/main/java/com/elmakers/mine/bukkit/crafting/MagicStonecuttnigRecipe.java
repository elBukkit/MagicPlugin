package com.elmakers.mine.bukkit.crafting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicStonecuttnigRecipe extends MagicRecipe {
    protected Recipe recipe;
    protected ItemData ingredient;
    protected String group;

    protected MagicStonecuttnigRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    public ItemStack load(ConfigurationSection configuration) {
        ItemStack item = super.load(configuration);
        if (item == null) {
            return null;
        }
        String materialKey = configuration.getString("ingredient");
        ingredient = controller.getOrCreateItem(materialKey);
        if (ingredient == null) {
            controller.getLogger().warning("Could not create stonecutting recipe ingredient: " + materialKey);
            return null;
        }
        recipe = CompatibilityLib.getCompatibilityUtils().createStonecuttingRecipe(key, item, ingredient.getItemStack(1), ignoreDamage);
        if (recipe != null && group != null && !group.isEmpty()) {
            CompatibilityLib.getCompatibilityUtils().setRecipeGroup(recipe, group);
        }
        return item;
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }
}
