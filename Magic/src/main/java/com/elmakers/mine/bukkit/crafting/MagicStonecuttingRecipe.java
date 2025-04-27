package com.elmakers.mine.bukkit.crafting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicStonecuttingRecipe extends MagicRecipe {
    protected Recipe recipe;
    protected ItemData ingredient;
    protected String group;

    protected MagicStonecuttingRecipe(String key, MagicController controller) {
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
        ItemStack ingredientItem = ingredient == null ? null : ingredient.getItemStack();
        if (CompatibilityLib.getItemUtils().isEmpty(ingredientItem)) {
            controller.getLogger().warning("Invalid " + getType() + " recipe ingredient " + materialKey);
            return null;
        }
        recipe = CompatibilityLib.getCompatibilityUtils().createStonecuttingRecipe(key, item, ingredientItem, ignoreDamage);
        if (recipe != null && group != null && !group.isEmpty()) {
            CompatibilityLib.getCompatibilityUtils().setRecipeGroup(recipe, group);
        }
        return item;
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    protected String getType() {
        return "stonecutting";
    }
}
