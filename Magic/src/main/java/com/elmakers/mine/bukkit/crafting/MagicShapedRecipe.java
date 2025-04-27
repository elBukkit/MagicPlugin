package com.elmakers.mine.bukkit.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicShapedRecipe extends MagicRecipe {

    private Material substitue;
    private String group;
    private ShapedRecipe recipe;
    private Map<Character, ItemData> ingredients = new HashMap<>();

    protected MagicShapedRecipe(String key, MagicController controller) {
        super(key, controller);
    }

    @Override
    public ItemStack load(ConfigurationSection configuration) {
        ItemStack item = super.load(configuration);
        if (item == null) {
            return null;
        }
        substitue = ConfigurationUtils.getMaterial(configuration, "substitute", null);
        group = configuration.getString("group", "");

        String vanillaItemKey = configuration.getString("vanilla");
        boolean isVanillaRecipe = vanillaItemKey != null && !vanillaItemKey.isEmpty() && !vanillaItemKey.equalsIgnoreCase("false");
        if (isVanillaRecipe) {
            ItemStack vanillaItem = item;
            if (!vanillaItemKey.equalsIgnoreCase("true")) {
                vanillaItem = controller.createItem(vanillaItemKey);
                if (vanillaItem == null) {
                    controller.getLogger().warning("Crafting recipe " + key + " specifies a vanilla recipe with an invalid item: " + vanillaItemKey);
                    vanillaItem = item;
                }
            }
            List<Recipe> recipes = controller.getPlugin().getServer().getRecipesFor(vanillaItem);
            if (recipes != null && !recipes.isEmpty()) {
                if (recipes.size() > 1) {
                    controller.getLogger().warning("Crafting recipe " + key + " specifies a vanilla recipe, but more than one recipe was found for: " + getOutputKey() + ". Only one version will be overridden.");
                }
                Recipe recipe = recipes.get(0);
                if (recipe instanceof ShapedRecipe) {
                    ShapedRecipe copyRecipe = (ShapedRecipe)recipe;
                    this.recipe = CompatibilityLib.getCompatibilityUtils().createShapedRecipe(key, item);
                    this.recipe.shape(copyRecipe.getShape());
                    for (Map.Entry<Character, ItemStack> entry : copyRecipe.getIngredientMap().entrySet()) {
                        char ingredientKey = entry.getKey();
                        ItemStack input = entry.getValue();
                        if (CompatibilityLib.getItemUtils().isEmpty(input)) {
                            input = new ItemStack(Material.AIR);
                        }
                        ItemData ingredient = controller.createItemData(input);
                        ItemStack ingredientItem = ingredient == null ? null : ingredient.getItemStack();
                        if (CompatibilityLib.getItemUtils().isEmpty(ingredientItem)) {
                            controller.getLogger().warning("Invalid " + getType() + " recipe ingredient " + ingredientKey);
                            return null;
                        }
                        ingredients.put(ingredientKey, ingredient);
                        if (!CompatibilityLib.getCompatibilityUtils().setRecipeIngredient(this.recipe, ingredientKey, ingredientItem, ignoreDamage)) {
                            controller.getLogger().warning("Unable to set recipe ingredient from vanilla ingredient: " + input);
                            return null;
                        }
                    }
                } else {
                    controller.getLogger().warning("Crafting recipe " + key + " specifies a shapeless vanilla recipe: " + getOutputKey());
                }
            } else {
                controller.getLogger().warning("Crafting recipe " + key + " specifies a vanilla recipe, but no recipe was found for: " + getOutputKey());
            }
        } else {
            ShapedRecipe shaped = CompatibilityLib.getCompatibilityUtils().createShapedRecipe(key, item);
            // To make it easier to override recipes, we're just going to ignore any unused ingredients
            Set<String> symbolsUsed = new HashSet<>();
            List<String> rows = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                String recipeRow = configuration.getString("row_" + i, "");
                if (recipeRow.length() > 0) {
                    rows.add(recipeRow);
                    // This split is hacky and will always add a space but that's ok since space is always a good ingredient
                    symbolsUsed.addAll(Arrays.asList(recipeRow.split("")));
                }
            }
            if (rows.size() > 0) {
                shaped = shaped.shape(rows.toArray(new String[0]));
                ConfigurationSection materials = configuration.getConfigurationSection("ingredients");
                if (materials == null) {
                    materials = configuration.getConfigurationSection("materials");
                }
                Set<String> keys = materials.getKeys(false);
                for (String key : keys) {
                    String materialKey = materials.getString(key);
                    // If we didn't actually use this ingredient, remove it so Spigot doesn't throw an exception
                    // We'll print a warning though
                    if (!symbolsUsed.contains(key)) {
                        controller.getLogger().warning("Ingredient is not used in recipe " + getKey() + ", this may be normal if overriding an existing recipe or on an older server version, but if it is a custom recipe this may be a mistake " + key + ": " + materialKey);
                        continue;
                    }
                    ItemData ingredient = controller.getOrCreateItem(materialKey);
                    ItemStack ingredientItem = ingredient == null ? null : ingredient.getItemStack();
                    if (CompatibilityLib.getItemUtils().isEmpty(ingredientItem)) {
                        controller.getLogger().warning("Invalid " + getType() + " recipe ingredient " + materialKey);
                        return null;
                    }
                    if (!CompatibilityLib.getCompatibilityUtils().setRecipeIngredient(shaped, key.charAt(0), ingredientItem, ignoreDamage)) {
                        controller.getLogger().warning("Unable to set recipe ingredient " + materialKey);
                        return null;
                    }
                    ingredients.put(key.charAt(0), ingredient);
                }

                recipe = shaped;
            }
        }
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
    public Material getSubstitute() {
        return substitue;
    }

    @Override
    public RecipeMatchType getMatchType(Recipe matchRecipe, ItemStack[] matrix) {
        if (recipe == null || matrix.length < 4) return RecipeMatchType.NONE;
        return isSameRecipe(matchRecipe) ? RecipeMatchType.MATCH : RecipeMatchType.NONE;
    }

    @Override
    protected String getType() {
        return "shaped";
    }
}
