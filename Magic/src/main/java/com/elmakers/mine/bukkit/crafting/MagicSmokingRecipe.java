package com.elmakers.mine.bukkit.crafting;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicSmokingRecipe extends MagicCookingRecipe {
    protected MagicSmokingRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    protected Recipe createRecipe(ItemStack item) {
        return CompatibilityLib.getCompatibilityUtils().createSmokingRecipe(key, item, ingredient.getItemStack(1), ignoreDamage, experience, cookingTime);
    }

    @Override
    protected String getType() {
        return "smoking";
    }
}
