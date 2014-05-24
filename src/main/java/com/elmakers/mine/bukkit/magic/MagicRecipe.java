package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicRecipe {
    private String outputKey;
    private Set<Material> ingredients = new HashSet<Material>();
    private Material outputType;
    private Material substitue;
    private Recipe recipe;
    private final MagicController controller;

    public MagicRecipe(MagicController controller) {
        this.controller = controller;
    }

    public boolean load(ConfigurationSection configuration) {
        outputKey = configuration.getString("output");
        substitue = ConfigurationUtils.getMaterial(configuration, "substitue", null);

        Wand wand = controller.createWand(outputKey);
        if (wand == null) {
            return false;
        }
        ItemStack wandItem = wand.getItem();
        // CompatibilityUtils.removeCustomData(wandItem);
        outputType = wandItem.getType();
        ShapedRecipe shaped = new ShapedRecipe(wandItem);
        String recipeRow1 = configuration.getString("row_1", "");
        String recipeRow2 = configuration.getString("row_2", "");
        String recipeRow3 = configuration.getString("row_3", "");
        List<String> rows = new ArrayList<String>();
        for (int i = 1; i <= 3; i++) {
            String recipeRow = configuration.getString("row_" + i, "");
            if (recipeRow.length() > 0) {
                rows.add(recipeRow);
            }
        }
        if (rows.size() == 0) {
            return false;
        }
        shaped = shaped.shape(rows.toArray(new String[0]));

        ConfigurationSection materials = configuration.getConfigurationSection("materials");
        Set<String> keys = materials.getKeys(false);
        for (String key : keys) {
            MaterialAndData mat = new MaterialAndData(materials.getString(key));
            ingredients.add(mat.getMaterial());
            shaped.setIngredient(key.charAt(0), mat.getMaterial());
        }

        recipe = shaped;

        return true;
    }

    public void register(Plugin plugin) {
        // Add our custom recipe if crafting is enabled
        if (recipe != null) {
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
        ItemStack item = controller.createWand(outputKey).getItem();
        // CompatibilityUtils.removeCustomData(item);
        // CompatibilityUtils.addGlow(item);
        return item;
    }
}
