package com.elmakers.mine.bukkit.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicShapelessRecipe extends MagicRecipe {
    private String group;
    private ShapelessRecipe recipe;

    protected MagicShapelessRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    public ItemStack load(ConfigurationSection configuration) {
        ItemStack item = super.load(configuration);
        if (item == null) {
            return null;
        }
        group = configuration.getString("group", "");
        List<ItemStack> ingredients = new ArrayList<>();
        Collection<String> ingredientKeys = ConfigurationUtils.getStringList(configuration, "ingredients");
        for (String ingredientKey : ingredientKeys) {
            ItemData ingredient = controller.getOrCreateItem(ingredientKey);
            ItemStack ingredientItem = ingredient == null ? null : ingredient.getItemStack();
            if (CompatibilityLib.getItemUtils().isEmpty(ingredientItem)) {
                controller.getLogger().warning("Invalid " + getType() + " recipe ingredient " + ingredientKey);
                return null;
            }
            ingredients.add(ingredientItem);
        }
        recipe = CompatibilityLib.getCompatibilityUtils().createShapelessRecipe(key, item, ingredients, ignoreDamage);
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
        return "shapeless";
    }
}
