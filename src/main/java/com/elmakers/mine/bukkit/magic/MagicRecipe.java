package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicRecipe {
    private String outputKey;
    private Set<Material> ingredients = new HashSet<Material>();
    private Material outputType;
    private Material substitue;
    private String outputItemType;
    private boolean disableDefaultRecipe;
    private ShapedRecipe recipe;
    private final MagicController controller;

    public MagicRecipe(MagicController controller) {
        this.controller = controller;
    }

    public boolean load(ConfigurationSection configuration) {
        outputKey = configuration.getString("output");
        substitue = ConfigurationUtils.getMaterial(configuration, "substitue", null);
        disableDefaultRecipe = configuration.getBoolean("disable_default", false);

        outputItemType = configuration.getString("output_type", "");
        ItemStack item = null;

        if (outputItemType.equalsIgnoreCase("wand"))
        {
            Wand wand = (outputKey != null && !outputKey.isEmpty()) ? controller.createWand(outputKey) : null;
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
            MaterialAndData material = new MaterialAndData(outputKey);
            item = material.getItemStack(1);
        }
        else
        {
            // Backwards-compatibility
            outputType = ConfigurationUtils.getMaterial(configuration, "input", null);
            outputType = ConfigurationUtils.getMaterial(configuration, "output", outputType);
            return outputType != null;
        }

        if (item != null) {
            // CompatibilityUtils.removeCustomData(wandItem);
            outputType = item.getType();
            ShapedRecipe shaped = new ShapedRecipe(item);
            List<String> rows = new ArrayList<String>();
            for (int i = 1; i <= 3; i++) {
                String recipeRow = configuration.getString("row_" + i, "");
                if (recipeRow.length() > 0) {
                    rows.add(recipeRow);
                }
            }
            if (rows.size() > 0) {
                shaped = shaped.shape(rows.toArray(new String[0]));

                ConfigurationSection materials = configuration.getConfigurationSection("materials");
                Set<String> keys = materials.getKeys(false);
                for (String key : keys) {
                    String materialKey = materials.getString(key);
                    MaterialAndData mat = new MaterialAndData(materialKey);
                    ingredients.add(mat.getMaterial());
                    Material material = mat.getMaterial();
                    if (material == null) {
                        outputType = null;
                        controller.getLogger().warning("Unable to load recipe ingredient " + materialKey);
                        return false;
                    }
                    shaped.setIngredient(key.charAt(0), material);
                }

                recipe = shaped;
            }
        }
        return outputType != null;
    }

    public void register(Plugin plugin)
    {
        if (disableDefaultRecipe)
        {
            Iterator<Recipe> it = plugin.getServer().recipeIterator();
            while (it.hasNext())
            {
                Recipe defaultRecipe = it.next();
                if (defaultRecipe != null && defaultRecipe.getResult().getType() == outputType)
                {
                    plugin.getLogger().info("Disabled default crafting recipe for " + outputType);
                    it.remove();
                }
            }
        }
        // Add our custom recipe if crafting is enabled
        if (recipe != null)
        {
            plugin.getLogger().info("Adding crafting recipe for " + outputKey);
            plugin.getServer().addRecipe(recipe);
        }
    }

    public Material getOutputType() {
        return outputType;
    }

    public Set<Material> getIngredients() {
        return ingredients;
    }

    public Material getSubstitute() {
        return substitue;
    }

    public ItemStack craft() {
        if (outputKey == null) {
            return null;
        }
        ItemStack item = null;
        if (outputItemType.equalsIgnoreCase("wand"))
        {
            Wand wand = (outputKey != null && !outputKey.isEmpty()) ? controller.createWand(outputKey) : null;
            item = wand.getItem();
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
            MaterialAndData material = new MaterialAndData(outputKey);
            item = material.getItemStack(1);
        }

        return item;
    }

    public boolean isMatch(ItemStack[] matrix) {
        if (recipe == null || matrix.length < 9) return false;
        boolean rows[] = new boolean[3];
        boolean columns[] = new boolean[3];
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
        if (shape == null || shape.length < 1) return false;

        Map<Character, ItemStack> itemMap = recipe.getIngredientMap();
        int shapeRow = 0;
        for (int matrixRow = 0; matrixRow < 3; matrixRow++) {
            if (!rows[matrixRow]) continue;
            int shapeColumn = 0;
            for (int matrixColumn = 0; matrixColumn < 3; matrixColumn++) {
                if (!columns[matrixColumn]) continue;
                if (shapeRow >= shape.length) return false;

                String row = shape[shapeRow];
                char charAt = ' ';
                if (shapeColumn >= row.length()) {
                    return false;
                }
                charAt = row.charAt(shapeColumn);
                ItemStack item = itemMap.get(charAt);
                int i = matrixRow * 3 + matrixColumn;
                ItemStack ingredient = matrix[i];
                if (ingredient != null && ingredient.getType() == Material.AIR) {
                    ingredient = null;
                }
                if (item == null && ingredient == null) {
                    shapeColumn++;
                    continue;
                }
                if (item == null && ingredient != null) return false;
                if (ingredient == null && item != null) return false;
                if (ingredient.getType() != item.getType()) {
                    return false;
                }
                shapeColumn++;
            }
            shapeRow++;
        }
        return true;
    }
}
