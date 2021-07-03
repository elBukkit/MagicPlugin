package com.elmakers.mine.bukkit.crafting;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicBlastingRecipe extends MagicCookingRecipe {
    protected MagicBlastingRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    protected Recipe createRecipe(ItemStack item) {
        return CompatibilityLib.getCompatibilityUtils().createBlastingRecipe(key, item, ingredient.getItemStack(1), ignoreDamage, experience, cookingTime);
    }
}
