package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicRecipe {
    public enum MatchType { NONE, MATCH, PARTIAL }

    private String outputKey;
    private Material outputType;
    private Material substitue;
    private String outputItemType;
    private boolean disableDefaultRecipe;
    private ShapedRecipe recipe;
    private Map<Character, ItemData> ingredients = new HashMap<>();
    private final MagicController controller;
    private final NamespacedKey key;

    public static boolean FIRST_REGISTER = true;

    public MagicRecipe(String key, MagicController controller) {
        this.key = new NamespacedKey(controller.getPlugin(), key);
        this.controller = controller;
    }

    public boolean load(ConfigurationSection configuration) {
        outputKey = configuration.getString("output");

        if (outputKey == null) {
            return false;
        }

        substitue = ConfigurationUtils.getMaterial(configuration, "substitue", null);
        disableDefaultRecipe = configuration.getBoolean("disable_default", false);

        if (disableDefaultRecipe) {
            controller.getLogger().warning("Recipe " + key + " has disable_default: true, ignoring because trying to remove a recipe now throws an error.");
            disableDefaultRecipe = false;
        }

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
        else
        {
            return false;
        }

        if (item != null) {
            outputType = item.getType();
            ShapedRecipe shaped = new ShapedRecipe(key, item);
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
                    Material material = ingredient == null ? null : ingredient.getType();
                    if (material == null) {
                        outputType = null;
                        controller.getLogger().warning("Unable to load recipe ingredient " + materialKey);
                        return false;
                    }
                    shaped.setIngredient(key.charAt(0), material);
                    ingredients.put(key.charAt(0), ingredient);
                }

                recipe = shaped;
            }
        }
        return outputType != null;
    }

    public void register(Plugin plugin)
    {
        // I think we can only do this once..
        if (FIRST_REGISTER) {
            if (disableDefaultRecipe)
            {
                Iterator<Recipe> it = plugin.getServer().recipeIterator();
                while (it.hasNext())
                {
                    Recipe defaultRecipe = it.next();
                    if (defaultRecipe != null && defaultRecipe.getResult().getType() == outputType && defaultRecipe.getResult().getDurability() == 0)
                    {
                        plugin.getLogger().info("Disabled default crafting recipe for " + outputType);
                        it.remove();
                    }
                }
            }
        }
        // Add our custom recipe if crafting is enabled
        if (recipe != null)
        {
            if (!FIRST_REGISTER) {
               List<Recipe> existing = plugin.getServer().getRecipesFor(craft());
                if (existing.size() > 0) {
                    return;
                }
            }

            plugin.getLogger().info("Adding crafting recipe for " + outputKey);
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

    @SuppressWarnings("deprecation")
    public MatchType getMatchType(ItemStack[] matrix) {
        if (recipe == null || matrix.length < 9) return MatchType.NONE;
        boolean[] rows = new boolean[3];
        boolean[] columns = new boolean[3];
        for (int matrixRow = 0; matrixRow < 3; matrixRow++) {
            for (int matrixColumn = 0; matrixColumn < 3; matrixColumn++) {
                int i = matrixRow * 3 + matrixColumn;
                ItemStack ingredient = matrix[i];
                if (ingredient != null && ingredient.getType() != Material.AIR) {
                    rows[matrixRow] = true;
                    break;
                }
            }
        }
        for (int matrixColumn = 0; matrixColumn < 3; matrixColumn++) {
            for (int matrixRow = 0; matrixRow < 3; matrixRow++) {
                int i = matrixRow * 3 + matrixColumn;
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
        for (int matrixRow = 0; matrixRow < 3; matrixRow++) {
            if (!rows[matrixRow]) continue;
            int shapeColumn = 0;
            for (int matrixColumn = 0; matrixColumn < 3; matrixColumn++) {
                if (!columns[matrixColumn]) continue;
                if (shapeRow >= shape.length) return MatchType.NONE;

                String row = shape[shapeRow];
                char charAt = ' ';
                if (shapeColumn >= row.length()) {
                    return MatchType.NONE;
                }
                charAt = row.charAt(shapeColumn);
                ItemData item = ingredients.get(charAt);
                int i = matrixRow * 3 + matrixColumn;
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
                if (ingredient.getDurability() != item.getDurability()) {
                    return MatchType.NONE;
                }
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    ItemMeta ingredientMeta = ingredient.getItemMeta();
                    if (ingredientMeta == null) {
                        return MatchType.PARTIAL;
                    }

                    if (meta.hasDisplayName() && (!ingredientMeta.hasDisplayName() || !meta.getDisplayName().equals(ingredientMeta.getDisplayName()))) {
                        return MatchType.PARTIAL;
                    }

                    if (meta.hasLore() && (!ingredientMeta.hasLore() || !meta.getLore().equals(ingredientMeta.getLore()))) {
                        return MatchType.PARTIAL;
                    }
                }
                shapeColumn++;
            }
            shapeRow++;
        }
        return MatchType.MATCH;
    }

    public String getKey() {
        return key.getKey();
    }
}
