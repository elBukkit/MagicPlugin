package com.elmakers.mine.bukkit.magic;

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
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicRecipe {
    public enum MatchType { NONE, MATCH, PARTIAL }

    private String outputKey;
    private Material outputType;
    private Material substitue;
    private String outputItemType;
    private String group;
    private boolean ignoreDamage;
    private boolean disableDefaultRecipe;
    private ShapedRecipe recipe;
    private Map<Character, ItemData> ingredients = new HashMap<>();
    private final MagicController controller;
    private final String key;
    private boolean autoDiscover = false;
    private boolean locked = false;
    private List<String> discover = null;

    public static boolean FIRST_REGISTER = true;

    public MagicRecipe(String key, MagicController controller) {
        this.key = key;
        this.controller = controller;
    }

    public boolean load(ConfigurationSection configuration) {
        outputKey = configuration.getString("output");
        if (outputKey == null || outputKey.isEmpty()) {
            outputKey = this.key;
        }

        substitue = ConfigurationUtils.getMaterial(configuration, "substitute", null);
        disableDefaultRecipe = configuration.getBoolean("disable_default", false);
        group = configuration.getString("group", "");
        ignoreDamage = configuration.getBoolean("ignore_damage", false);
        autoDiscover = configuration.getBoolean("auto_discover", false);
        locked = configuration.getBoolean("locked", false);
        discover = ConfigurationUtils.getStringList(configuration, "discover");

        outputItemType = configuration.getString("output_type", "item");
        ItemStack item = null;

        if (outputItemType.equalsIgnoreCase("wand"))
        {
            Wand wand = !outputKey.isEmpty() ? controller.createWand(outputKey) : null;
            if (wand != null) {
                item = wand.getItem();
            } else {
                controller.getLogger().warning("Unable to load recipe output wand: " + outputKey);
            }
        }
        else if (outputItemType.equalsIgnoreCase("spell"))
        {
            item = controller.createSpellItem(outputKey);
        }
        else if (outputItemType.equalsIgnoreCase("brush"))
        {
            item = controller.createBrushItem(outputKey);
        }
        else if (outputItemType.equalsIgnoreCase("item"))
        {
            item = controller.createItem(outputKey);
        }
        if (item == null) {
            controller.getLogger().warning("Unknown output for recipe " + key + ": " + outputKey);
            return false;
        }

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
            outputType = item.getType();
            List<Recipe> recipes = controller.getPlugin().getServer().getRecipesFor(vanillaItem);
            if (recipes != null && !recipes.isEmpty()) {
                if (recipes.size() > 1) {
                    controller.getLogger().warning("Crafting recipe " + key + " specifies a vanilla recipe, but more than one recipe was found for: " + outputKey + ". Only one version will be overridden.");
                }
                Recipe recipe = recipes.get(0);
                if (recipe instanceof ShapedRecipe) {
                    ShapedRecipe copyRecipe = (ShapedRecipe)recipe;
                    this.recipe = CompatibilityLib.getCompatibilityUtils().createShapedRecipe(controller.getPlugin(), key, item);
                    this.recipe.shape(copyRecipe.getShape());
                    for (Map.Entry<Character, ItemStack> entry : copyRecipe.getIngredientMap().entrySet()) {
                        char ingredientKey = entry.getKey();
                        ItemStack input = entry.getValue();
                        if (ItemUtils.isEmpty(input)) {
                            input = new ItemStack(Material.AIR);
                        }
                        ItemData ingredient = controller.createItemData(input);
                        ingredients.put(ingredientKey, ingredient);
                        if (!CompatibilityLib.getCompatibilityUtils().setRecipeIngredient(this.recipe, ingredientKey, ingredient.getItemStack(1), ignoreDamage)) {
                            outputType = null;
                            controller.getLogger().warning("Unable to set recipe ingredient from vanilla ingredient: " + input);
                            return false;
                        }
                    }
                } else {
                    controller.getLogger().warning("Crafting recipe " + key + " specifies a shapeless vanilla recipe: " + outputKey);
                }
            } else {
                controller.getLogger().warning("Crafting recipe " + key + " specifies a vanilla recipe, but no recipe was found for: " + outputKey);
            }
        } else {
            outputType = item.getType();
            ShapedRecipe shaped = CompatibilityLib.getCompatibilityUtils().createShapedRecipe(controller.getPlugin(), key, item);
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
                        outputType = null;
                        controller.getLogger().warning("Invalid recipe ingredient " + materialKey);
                        return false;
                    }
                    if (!CompatibilityLib.getCompatibilityUtils().setRecipeIngredient(shaped, key.charAt(0), ingredient.getItemStack(1), ignoreDamage)) {
                        outputType = null;
                        controller.getLogger().warning("Unable to set recipe ingredient " + materialKey);
                        return false;
                    }
                    ingredients.put(key.charAt(0), ingredient);
                }

                recipe = shaped;
            }
        }
        if (recipe != null && group != null && !group.isEmpty()) {
            CompatibilityLib.getCompatibilityUtils().setRecipeGroup(recipe, group);
        }
        return outputType != null;
    }

    public void unregister(Plugin plugin) {
        // Remove this recipe
        boolean canRemoveRecipes = CompatibilityLib.getCompatibilityUtils().canRemoveRecipes();
        if (recipe != null && !FIRST_REGISTER && canRemoveRecipes) {
            CompatibilityLib.getCompatibilityUtils().removeRecipe(plugin, key);
        }
    }

    public void preregister(Plugin plugin) {
        boolean canRemoveRecipes = CompatibilityLib.getCompatibilityUtils().canRemoveRecipes();
        if (disableDefaultRecipe && canRemoveRecipes) {
            int disabled = 0;
            List<Recipe> existing = plugin.getServer().getRecipesFor(new ItemStack(outputType));
            for (Recipe recipe : existing) {
                CompatibilityLib.getCompatibilityUtils().removeRecipe(plugin, recipe);
                disabled++;
            }
            if (disabled > 0) {
                plugin.getLogger().info("Disabled " + disabled + " default crafting recipe(s) for " + outputType);
            }
        }
    }

    public void register(MagicController controller, Plugin plugin)
    {
        boolean canRemoveRecipes = CompatibilityLib.getCompatibilityUtils().canRemoveRecipes();
        // Add our custom recipe if crafting is enabled
        if (recipe != null)
        {
            // Recipes can't be removed on older minecraft versions, so we have to skip re-registering if we've already registered this one
            if (!FIRST_REGISTER && !canRemoveRecipes) {
                List<Recipe> existing = plugin.getServer().getRecipesFor(craft());
                if (existing.size() > 0) {
                    return;
                }
            }
            controller.info("Adding crafting recipe for " + outputKey);
            try {
                plugin.getServer().addRecipe(recipe);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to add recipe", ex);
            }
        }
    }

    public Material getOutputType() {
        return outputType;
    }

    public Material getSubstitute() {
        return substitue;
    }

    public @Nullable ItemStack craft() {
        if (outputKey == null) {
            return null;
        }

        ItemStack item;
        if (outputItemType.equalsIgnoreCase("wand")) {
            if (outputKey != null && !outputKey.isEmpty()) {
                item = controller.createWand(outputKey).getItem();
            } else {
                item = null;
            }
        }
        else if (outputItemType.equalsIgnoreCase("spell"))
        {
            item = controller.createSpellItem(outputKey);
        }
        else if (outputItemType.equalsIgnoreCase("brush"))
        {
            item = controller.createBrushItem(outputKey);
        }
        else if (outputItemType.equalsIgnoreCase("item"))
        {
            item = controller.createItem(outputKey);
        }
        else
        {
            item = null;
        }

        return item;
    }

    public boolean isSameRecipe(Recipe matchRecipe) {
        return CompatibilityLib.getCompatibilityUtils().isSameKey(controller.getPlugin(), getKey(), matchRecipe);
    }

    @SuppressWarnings("deprecation")
    public MatchType getMatchType(Recipe matchRecipe, ItemStack[] matrix) {
        if (recipe == null || matrix.length < 4) return MatchType.NONE;

        // Modern minecraft versions account for custom data in ingredients,
        // so we can leave the ingredient matching up to vanilla code.
        // The complicated matrix matching code here is flawed, for instance it does not
        // account for vanilla recipes being mirrorable.
        if (!CompatibilityLib.getCompatibilityUtils().isLegacyRecipes()) {
            return isSameRecipe(matchRecipe) ? MatchType.MATCH : MatchType.NONE;
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
        if (shape == null || shape.length < 1) return MatchType.NONE;

        int shapeRow = 0;
        for (int matrixRow = 0; matrixRow < height; matrixRow++) {
            if (!rows[matrixRow]) continue;
            int shapeColumn = 0;
            for (int matrixColumn = 0; matrixColumn < width; matrixColumn++) {
                if (!columns[matrixColumn]) continue;
                if (shapeRow >= shape.length) return MatchType.NONE;

                String row = shape[shapeRow];
                char charAt = ' ';
                if (shapeColumn >= row.length()) {
                    return MatchType.NONE;
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
                if (item == null && ingredient != null) return MatchType.NONE;
                if (ingredient == null && item != null) return MatchType.NONE;
                if (ingredient.getType() != item.getType()) {
                    return MatchType.NONE;
                }
                if (!ignoreDamage && ingredient.getDurability() != item.getDurability()) {
                    return MatchType.NONE;
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
                        return MatchType.PARTIAL;
                    }
                }
                shapeColumn++;
            }
            shapeRow++;
        }
        return MatchType.MATCH;
    }

    public void crafted(HumanEntity entity, MageController controller) {
        if (discover == null) return;
        for (String key : discover) {
            if (controller.hasPermission(entity, "Magic.craft." + key)) {
                CompatibilityLib.getCompatibilityUtils().discoverRecipe(entity, controller.getPlugin(), key);
            }
        }
    }

    public String getKey() {
        return key;
    }

    public boolean isAutoDiscover() {
        return autoDiscover;
    }

    public boolean isLocked() {
        return locked;
    }

}
