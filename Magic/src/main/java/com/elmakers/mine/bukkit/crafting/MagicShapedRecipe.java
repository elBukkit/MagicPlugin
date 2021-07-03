package com.elmakers.mine.bukkit.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
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
                        ingredients.put(ingredientKey, ingredient);
                        if (!CompatibilityLib.getCompatibilityUtils().setRecipeIngredient(this.recipe, ingredientKey, ingredient.getItemStack(1), ignoreDamage)) {
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
            List<String> rows = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                String recipeRow = configuration.getString("row_" + i, "");
                if (recipeRow.length() > 0) {
                    rows.add(recipeRow);
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
                    ItemData ingredient = controller.getOrCreateItem(materialKey);
                    if (ingredient == null) {
                        controller.getLogger().warning("Invalid recipe ingredient " + materialKey);
                        return null;
                    }
                    if (!CompatibilityLib.getCompatibilityUtils().setRecipeIngredient(shaped, key.charAt(0), ingredient.getItemStack(1), ignoreDamage)) {
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
    @SuppressWarnings("deprecation")
    public RecipeMatchType getMatchType(Recipe matchRecipe, ItemStack[] matrix) {
        if (recipe == null || matrix.length < 4) return RecipeMatchType.NONE;

        // Modern minecraft versions account for custom data in ingredients,
        // so we can leave the ingredient matching up to vanilla code.
        // The complicated matrix matching code here is flawed, for instance it does not
        // account for vanilla recipes being mirrorable.
        if (!CompatibilityLib.getCompatibilityUtils().isLegacyRecipes()) {
            return isSameRecipe(matchRecipe) ? RecipeMatchType.MATCH : RecipeMatchType.NONE;
        }
        int height = (int)Math.sqrt(matrix.length);
        int width = height;
        boolean[] rows = new boolean[width];
        boolean[] columns = new boolean[height];
        for (int matrixRow = 0; matrixRow < height; matrixRow++) {
            for (int matrixColumn = 0; matrixColumn < width; matrixColumn++) {
                int i = matrixRow * height + matrixColumn;
                ItemStack ingredient = matrix[i];
                if (ingredient != null && ingredient.getType() != Material.AIR) {
                    rows[matrixRow] = true;
                    break;
                }
            }
        }
        for (int matrixColumn = 0; matrixColumn < width; matrixColumn++) {
            for (int matrixRow = 0; matrixRow < height; matrixRow++) {
                int i = matrixRow * width + matrixColumn;
                ItemStack ingredient = matrix[i];
                if (ingredient != null && ingredient.getType() != Material.AIR) {
                    columns[matrixColumn] = true;
                    break;
                }
            }
        }

        String[] shape = recipe.getShape();
        if (shape == null || shape.length < 1) return RecipeMatchType.NONE;

        int shapeRow = 0;
        for (int matrixRow = 0; matrixRow < height; matrixRow++) {
            if (!rows[matrixRow]) continue;
            int shapeColumn = 0;
            for (int matrixColumn = 0; matrixColumn < width; matrixColumn++) {
                if (!columns[matrixColumn]) continue;
                if (shapeRow >= shape.length) return RecipeMatchType.NONE;

                String row = shape[shapeRow];
                char charAt = ' ';
                if (shapeColumn >= row.length()) {
                    return RecipeMatchType.NONE;
                }
                charAt = row.charAt(shapeColumn);
                ItemData item = ingredients.get(charAt);
                int i = matrixRow * width + matrixColumn;
                ItemStack ingredient = matrix[i];
                if (ingredient != null && ingredient.getType() == Material.AIR) {
                    ingredient = null;
                }
                if (item == null && ingredient == null) {
                    shapeColumn++;
                    continue;
                }
                if (item == null && ingredient != null) return RecipeMatchType.NONE;
                if (ingredient == null && item != null) return RecipeMatchType.NONE;
                if (ingredient.getType() != item.getType()) {
                    return RecipeMatchType.NONE;
                }
                if (!ignoreDamage && ingredient.getDurability() != item.getDurability()) {
                    return RecipeMatchType.NONE;
                }
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    ItemStack compareItem = item.getItemStack();
                    ItemStack ingredientItem = ingredient;
                    if (ignoreDamage && compareItem.getDurability() != ingredientItem.getDurability()) {
                        ingredientItem = ingredientItem.clone();
                        ingredientItem.setDurability(compareItem.getDurability());
                    }
                    if (!controller.itemsAreEqual(ingredientItem, compareItem)) {
                        return RecipeMatchType.PARTIAL;
                    }
                }
                shapeColumn++;
            }
            shapeRow++;
        }
        return RecipeMatchType.MATCH;
    }

    @Override
    protected String getType() {
        return "shaped";
    }
}
