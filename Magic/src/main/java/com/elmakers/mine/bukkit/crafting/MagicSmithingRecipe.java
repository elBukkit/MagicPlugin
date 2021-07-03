package com.elmakers.mine.bukkit.crafting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicSmithingRecipe extends MagicRecipe {
    protected Recipe recipe;
    protected ItemData ingredient;
    protected ItemData addition;
    protected String group;

    protected MagicSmithingRecipe(String key, MagicController controller) {
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
            controller.getLogger().warning("Could not create smithing recipe ingredient: " + materialKey);
            return null;
        }
        materialKey = configuration.getString("addition");
        addition = controller.getOrCreateItem(materialKey);
        if (addition == null) {
            controller.getLogger().warning("Could not create smithing recipe addition: " + materialKey);
            return null;
        }
        recipe = CompatibilityLib.getCompatibilityUtils().createSmithingRecipe(key, item, ingredient.getItemStack(1), addition.getItemStack(1));
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
        return "smithing";
    }
}
