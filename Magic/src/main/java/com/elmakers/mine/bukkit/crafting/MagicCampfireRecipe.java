package com.elmakers.mine.bukkit.crafting;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicCampfireRecipe extends MagicCookingRecipe {
    protected MagicCampfireRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    protected Recipe createRecipe(ItemStack item) {
        ItemStack ingredientItem = ingredient == null ? null : ingredient.getItemStack();
        if (CompatibilityLib.getItemUtils().isEmpty(ingredientItem)) {
            controller.getLogger().warning("Invalid "  + getType() + " recipe ingredient " + ingredientKey);
            return null;
        }
        return CompatibilityLib.getCompatibilityUtils().createCampfireRecipe(key, item, ingredientItem, ignoreDamage, experience, cookingTime);
    }

    @Override
    protected String getType() {
        return "campfire";
    }
}
