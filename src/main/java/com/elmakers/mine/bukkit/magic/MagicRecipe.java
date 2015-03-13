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
import java.util.Set;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public class MagicRecipe {
    private String outputKey;
    private Set<Material> ingredients = new HashSet<Material>();
    private Material outputType;
    private Material substitue;
    private boolean disableDefaultRecipe;
    private Recipe recipe;
    private final MagicController controller;

    public MagicRecipe(MagicController controller) {
        this.controller = controller;
    }

    public boolean load(ConfigurationSection configuration) {
        outputKey = configuration.getString("output");
        substitue = ConfigurationUtils.getMaterial(configuration, "substitue", null);
        disableDefaultRecipe = configuration.getBoolean("disable_default", false);

        Wand wand = (outputKey != null && !outputKey.isEmpty()) ? controller.createWand(outputKey) : null;
        if (wand != null) {
            ItemStack wandItem = wand.getItem();
            // CompatibilityUtils.removeCustomData(wandItem);
            outputType = wandItem.getType();
            ShapedRecipe shaped = new ShapedRecipe(wandItem);
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
                    MaterialAndData mat = new MaterialAndData(materials.getString(key));
                    ingredients.add(mat.getMaterial());
                    shaped.setIngredient(key.charAt(0), mat.getMaterial());
                }

                recipe = shaped;
            }
        }

        if (outputType == null) {
            outputType = ConfigurationUtils.getMaterial(configuration, "input", null);
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
        ItemStack item = controller.createWand(outputKey).getItem();
        // CompatibilityUtils.removeCustomData(item);
        // CompatibilityUtils.addGlow(item);
        return item;
    }
}
